package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;
import pl.airr.cinderac.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * State of a single player. Packets are processed on the netty thread under this object's lock,
 * Bukkit events (owner thread) take the same lock.
 */
public final class PlayerData {
    public final User user;
    public volatile Player player;
    public final UUID uuid;
    public final String name;
    public volatile String clientBrand = "Unresolved";
    public int entityId = -1;
    public final long joinTime = System.currentTimeMillis();
    public final int joinTick;

    public final PositionTracker position = new PositionTracker(this);
    public final ActionTracker action = new ActionTracker(this);
    public final RotationTracker rotation = new RotationTracker(this);
    public final CombatTracker combat = new CombatTracker(this);
    public final VelocityTracker velocity = new VelocityTracker(this);
    public final TransactionTracker transactions = new TransactionTracker(this);
    public final KeepAliveTracker keepAlive = new KeepAliveTracker(this);
    public final ClickTracker clicks = new ClickTracker(this);
    public final List<Check> checks = new ArrayList<>();

    public int totalViolations, combatViolations, movementViolations, playerViolations, autoClickerViolations;
    public int scaffoldViolations, timerViolations;
    public volatile boolean joinConfirmed, kicked, pendingSetback, frozen;
    public long lastPunishTime, lastFlagTime = -1L;
    private boolean brandHandled;

    /** Last packets of each type (checks compare ordering). */
    public WrapperPlayClientPlayerFlying lastFlying;
    public WrapperPlayClientInteractEntity lastInteract;
    public WrapperPlayClientPlayerDigging lastDigging;
    public WrapperPlayClientEntityAction lastEntityAction;
    public WrapperPlayClientHeldItemChange lastHeldItemChange;
    public WrapperPlayClientPlayerBlockPlacement lastBlockPlace;

    /** Ids of transactions sent before JOIN_GAME was confirmed - their replies are ignored. */
    private final Set<Integer> preJoinIds = ConcurrentHashMap.newKeySet();

    public PlayerData(User user) {
        this.user = user;
        this.uuid = user.getUUID();
        this.name = user.getName();
        this.joinTick = CinderAC.get().tick();
    }

    public ClientVersion clientVersion() {
        ClientVersion v = user.getClientVersion();
        return v == null ? ClientVersion.UNKNOWN : v;
    }

    public boolean exempt(Exempt... types) {
        for (Exempt e : types) if (e.test(this)) return true;
        return false;
    }

    /** Ping in ticks (from transactions) + 3 of tolerance. */
    public int pingTicks() {
        return (int) Math.floor(transactions.transactionPing / 50.0) + 3;
    }

    public boolean isBedrock() {
        Settings s = Settings.get();
        return s.ignoreGeyserUuids && uuid.toString().startsWith("00000")
                || s.ignoreGeyserPrefixes && name.startsWith(s.geyserPrefix)
                || clientBrand.toLowerCase().contains("geyser");
    }

    public boolean newClient() {
        return clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
    }

    /** Client and server are 1.21.2+ (CLIENT_TICK_END exists). */
    public boolean hasTickEnd() {
        return clientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2) && ServerVersion.atLeast(21, 2);
    }

    // --- actions on the player (owner thread) ---

    public void kick(String message) {
        kicked = true;
        Player p = player;
        if (p == null) {
            user.closeConnection();
            return;
        }
        String m = Text.color(message);
        CinderAC.get().tasks().runForPlayer(p, () -> p.kickPlayer(m));
    }

    public void teleport(Location loc) {
        Player p = player;
        if (p == null) return;
        CinderAC.get().tasks().runForPlayer(p, () -> {
            if (ServerVersion.isFolia()) p.teleportAsync(loc);
            else p.teleport(loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.UNKNOWN);
        });
    }

    public void rotateRandomly() {
        Player p = player;
        if (p == null) return;
        CinderAC.get().tasks().runForPlayer(p, () -> {
            Location l = p.getLocation();
            l.setYaw((float) ThreadLocalRandom.current().nextDouble(360.0) - 180.0f);
            l.setPitch((float) ThreadLocalRandom.current().nextDouble(180.0) - 90.0f);
            if (ServerVersion.isFolia()) p.teleportAsync(l);
            else p.teleport(l, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.UNKNOWN);
        });
    }

    public void sendMessage(String message) {
        Player p = player;
        if (p != null) p.sendMessage(Text.color(Settings.get().withPrefix(message)));
    }

    // --- client packets ---

    public synchronized void handleReceive(PacketReceiveEvent event, long time) {
        Packet packet = new Packet(event, time);
        PacketType.Play.Client type = (PacketType.Play.Client) event.getPacketType();
        switch (type) {
            case PLAYER_FLYING:
            case PLAYER_POSITION:
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION: {
                WrapperPlayClientPlayerFlying f = packet.flying();
                lastFlying = f;
                // 1.17+ clients send a duplicate position when using an item - skipped entirely
                if (clientVersion().isNewerThanOrEquals(ClientVersion.V_1_17) && f.hasPositionChanged() && f.hasRotationChanged()
                        && f.getLocation().getX() == position.x && f.getLocation().getY() == position.y && f.getLocation().getZ() == position.z) {
                    return;
                }
                if (player != null && !brandHandled) handleBrand();
                combat.handleFlying();
                keepAlive.handleFlying();
                position.handleFlying(f);
                action.handleFlying(f);
                velocity.handleFlying(f);
                transactions.handleFlying();
                if (f.hasRotationChanged()) rotation.handleRotation(f.getLocation().getYaw(), f.getLocation().getPitch());
                break;
            }
            case PONG: {
                int id = new WrapperPlayClientPong(event).getId();
                if (!handleConfirmation(id)) return;
                break;
            }
            case WINDOW_CONFIRMATION: {
                int id = new WrapperPlayClientWindowConfirmation(event).getActionId();
                if (!handleConfirmation(id)) return;
                break;
            }
            case ENTITY_ACTION:
                lastEntityAction = packet.entityAction();
                action.handleEntityAction(lastEntityAction);
                break;
            case PLAYER_DIGGING:
                lastDigging = packet.digging();
                action.handleDigging(lastDigging);
                position.handleDigging(lastDigging);
                break;
            case HELD_ITEM_CHANGE:
                lastHeldItemChange = packet.heldItemChange();
                break;
            case VEHICLE_MOVE:
                position.handleVehicleMove(packet.vehicleMove());
                break;
            case CLIENT_STATUS:
                action.handleClientStatus(new WrapperPlayClientClientStatus(event));
                break;
            case KEEP_ALIVE:
                keepAlive.handleKeepAlive();
                break;
            case PLAYER_BLOCK_PLACEMENT:
                lastBlockPlace = packet.blockPlace();
                action.handleBlockPlacePacket();
                break;
            case ATTACK:
            case INTERACT_ENTITY:
                lastInteract = packet.interact();
                action.handleInteract(lastInteract);
                combat.handleInteract(lastInteract);
                break;
            case CLICK_WINDOW:
                action.handleWindowClick();
                break;
            case ANIMATION:
                clicks.handleAnimation();
                position.onFireworkHeld(player);
                action.handleAnimation();
                break;
            default:
                break;
        }
        runChecks(packet);
        if (packet.isFlying()) afterFlying(packet.flying());
    }

    /** false = the packet is not our transaction / arrived before JOIN_GAME and is not passed to checks. */
    private boolean handleConfirmation(int id) {
        if (id > 0) return false;
        if (preJoinIds.remove(id)) return false;
        if (id == transactions.joinId && !joinConfirmed) {
            transactions.lastPongTime = System.currentTimeMillis();
            joinConfirmed = true;
            return false;
        }
        transactions.onPong(id);
        return true;
    }

    private void runChecks(Packet packet) {
        for (Check c : checks) {
            try {
                c.handle(packet);
            } catch (RuntimeException e) {
                CinderAC.get().checkError(c, e);
            }
        }
    }

    private void afterFlying(WrapperPlayClientPlayerFlying f) {
        Material below = position.below1;
        if (!frozen && below != null && !Blocks.isAir(below) && below.isBlock() && position.mathematicallyOnGround
                && !Blocks.LIQUID.is(below) && position.sinceSetbackTicks > 5 && position.deltaXZ < 5.0
                && !action.acceptedServerPosition && position.sinceGhostBlockTicks > 20) {
            position.setbackX = position.x;
            position.setbackY = position.y;
            position.setbackZ = position.z;
        }
        if (pendingSetback) {
            if (player == null) return;
            teleport(new Location(position.world, position.setbackX, position.setbackY, position.setbackZ, rotation.yaw, rotation.pitch));
            pendingSetback = false;
        }
        if (f.hasPositionChanged()) position.flushAfterPosition();
    }

    private void handleBrand() {
        String brand = CinderAC.get().players().takeBrand(uuid);
        Player p = player;
        if (brand == null || p == null) return;
        brandHandled = true;
        clientBrand = brand;
        Settings s = Settings.get();
        if (brand.length() > 55 || brand.contains("\n") || brand.contains("%") || brand.contains("$")) {
            kick("&cInvalid client brand.");
            return;
        }
        if (!p.hasPermission("cinderac.bypass.client-brand")) {
            for (String b : s.brandBlocked) {
                if (brand.toLowerCase().contains(b.toLowerCase())) {
                    kick(s.brandKickMessage.replace("%client-brand%", brand));
                    return;
                }
            }
            if (s.brandWhitelist && !p.hasPermission("cinderac.bypass.client-brand.whitelist")) {
                boolean ok = false;
                for (String w : s.brandWhitelisted) ok |= brand.toLowerCase().contains(w.toLowerCase());
                if (!ok) {
                    kick(s.brandKickMessage.replace("%client-brand%", brand));
                    return;
                }
            }
        }
        if (s.brandAlerts) {
            for (String ignore : s.brandIgnore) if (brand.toLowerCase().contains(ignore.toLowerCase())) return;
            String msg = s.brandMessage.replace("%player%", name).replace("%client-version%", clientVersion().getReleaseName())
                    .replace("%client-brand%", brand);
            if (s.brandConsole) CinderAC.get().getLogger().info(Text.strip(s.brandConsoleMessage.replace("%player%", name).replace("%client-brand%", brand)));
            if (!p.hasPermission("cinderac.bypass.client-brand.alert")) CinderAC.get().alerts().staff(msg);
        }
    }

    // --- server packets ---

    public synchronized void handleSend(PacketSendEvent event) {
        PacketType.Play.Server type = (PacketType.Play.Server) event.getPacketType();
        switch (type) {
            case JOIN_GAME: {
                entityId = new WrapperPlayServerJoinGame(event).getEntityId();
                event.getTasksAfterSend().add(() -> {
                    short id = (short) ThreadLocalRandom.current().nextInt(-11500, -11000);
                    transactions.joinId = id;
                    transactions.sendPing(id);
                });
                break;
            }
            case PING: {
                int id = new WrapperPlayServerPing(event).getId();
                if (id > 0) return;
                if (!joinConfirmed && id != transactions.joinId) {
                    preJoinIds.add(id);
                    return;
                }
                transactions.onSent(id);
                break;
            }
            case WINDOW_CONFIRMATION: {
                WrapperPlayServerWindowConfirmation w = new WrapperPlayServerWindowConfirmation(event);
                int id = w.getActionId();
                if (id > 0) return;
                if (!joinConfirmed && id != transactions.joinId) {
                    preJoinIds.add(id);
                    return;
                }
                if (w.isAccepted() || !joinConfirmed) break;
                transactions.onSent(id);
                break;
            }
            case ENTITY_VELOCITY: {
                WrapperPlayServerEntityVelocity w = new WrapperPlayServerEntityVelocity(event);
                if (w.getEntityId() != entityId) break;
                velocity.handleVelocity(w.getVelocity(), () -> event.getTasksAfterSend().add(() -> transactions.sendConfirmation(false)));
                break;
            }
            case EXPLOSION: {
                WrapperPlayServerExplosion w = new WrapperPlayServerExplosion(event);
                velocity.handleExplosion(w.getPlayerMotion(), w.getKnockback(), () -> event.getTasksAfterSend().add(() -> transactions.sendConfirmation(false)));
                break;
            }
            case PLAYER_POSITION_AND_LOOK: {
                WrapperPlayServerPlayerPositionAndLook w = new WrapperPlayServerPlayerPositionAndLook(event);
                action.handleServerPosition(w, () -> event.getTasksAfterSend().add(() -> transactions.sendConfirmation(false)));
                combat.clearHistory();
                Packet packet = new Packet(event, System.currentTimeMillis());
                runChecks(packet);
                break;
            }
            case ENTITY_METADATA: {
                WrapperPlayServerEntityMetadata w = new WrapperPlayServerEntityMetadata(event);
                if (w.getEntityId() != entityId) break;
                action.handleMetadata(w);
                break;
            }
            case UPDATE_ATTRIBUTES: {
                WrapperPlayServerUpdateAttributes w = new WrapperPlayServerUpdateAttributes(event);
                if (w.getEntityId() != entityId) break;
                action.handleUpdateAttributes(w);
                break;
            }
            case PLAYER_ABILITIES: {
                WrapperPlayServerPlayerAbilities w = new WrapperPlayServerPlayerAbilities(event);
                action.sinceAbilitiesTicks = 0;
                transactions.onLastTransactionConfirm(() -> position.handleAbilities(w));
                break;
            }
            case HELD_ITEM_CHANGE:
                action.sinceServerSlotTicks = 0;
                break;
            case ENTITY_EFFECT: {
                WrapperPlayServerEntityEffect w = new WrapperPlayServerEntityEffect(event);
                if (w.getEntityId() != entityId) break;
                action.handleEntityEffect(w);
                break;
            }
            case REMOVE_ENTITY_EFFECT: {
                WrapperPlayServerRemoveEntityEffect w = new WrapperPlayServerRemoveEntityEffect(event);
                if (w.getEntityId() != entityId) break;
                action.handleRemoveEntityEffect(w);
                break;
            }
            case CHANGE_GAME_STATE:
                action.handleGameState(new WrapperPlayServerChangeGameState(event));
                break;
            case ENTITY_RELATIVE_MOVE: {
                WrapperPlayServerEntityRelativeMove w = new WrapperPlayServerEntityRelativeMove(event);
                combat.handleEntityMove(w.getEntityId(), w.getDeltaX(), w.getDeltaY(), w.getDeltaZ());
                break;
            }
            case ENTITY_RELATIVE_MOVE_AND_ROTATION: {
                WrapperPlayServerEntityRelativeMoveAndRotation w = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                combat.handleEntityMove(w.getEntityId(), w.getDeltaX(), w.getDeltaY(), w.getDeltaZ());
                break;
            }
            default:
                break;
        }
    }

    // --- Bukkit events ---

    public synchronized void handleBukkit(Event event) {
        for (Check c : checks) {
            try {
                c.handleBukkit(event);
            } catch (RuntimeException e) {
                CinderAC.get().checkError(c, e);
            }
        }
    }
}
