package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.VMath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Player actions (sprint, digging, placing), potion effects and attributes, damage, server teleports. */
public final class ActionTracker {
    private final PlayerData data;

    public boolean sprinting, sneaking, sentEntityActionThisTick, placedThisTick, digging, releasedUseItemThisTick;
    public boolean diggingThisTick, acceptedServerPosition;
    public boolean hasSpeed, hasJumpBoost, hasLevitation, hasSlowFalling, hasDolphinsGrace, hasConduitPower, hasSlowness, hasHaste;
    public boolean depthStrider, aquaAffinity, swiftSneak, wearingElytra, glidingBukkit, bridging, raining, instaBreak, mcmmoAbility;
    public boolean gliding, swimming, riptiding;
    /** A 1.9 client started gliding but the server did not yet - set by checks, fixed in PlayerMoveEvent. */
    public boolean pendingGlideFix, pendingSwimFix;
    public double movementSpeedAttribute = 0.1, stepHeightAttribute = 0.6, jumpStrengthAttribute = 0.42, gravityAttribute = 0.08;
    public double lastJumpStrength = 0.42, lastGravity = 0.08, interactionRange = 3.0;
    public final LinkedList<Vector> pendingServerPositions = new LinkedList<>();

    public int lastInteractTick, lastDropTick, lastDigSwingTick, ticksNotSprinting, positionTicks, lastWindowClickTick;
    public int placeItemAmount, lastPlaceItemAmount, placeSlot, lastPlaceSlot;
    public int sinceServerPositionTicks = 100, lastBedEnterTick, lastBedLeaveTick, lastPickupTick, lastCancelledBreakTick;
    public int lastDamageTick, sinceDragonDamageTicks = 100, sprintingTicks, lastRespawnTick;
    public int lastProjectileLaunchTick, lastFishTick;
    public int sinceFireDamageTicks = 100, sinceAttackDamageTicks = 100, sinceFallDamageTicks = 100, sinceMagicDamageTicks;
    public int sincePoisonDamageTicks, sinceContactDamageTicks, sinceProjectileDamageTicks, sinceLavaDamageTicks;
    public int sinceBucketTicks = 100, sincePlaceBlockTicks = 100, sincePlaceClimbableTicks = 100, sincePlaceSlimeTicks = 150;
    public int sincePlaceTicks = 100, sinceWorldChangeTicks = 100, sinceTotemTicks, sinceTeleportTicks = 100, sinceDeathTicks = 100;
    public int sinceNonFallDamageTicks = 100, sinceIronGolemDamageTicks, counterF, counterT, sinceGlassBottleTicks = 100;
    public int sinceFireballDamageTicks = 100;
    public int speedAmplifier, jumpBoostAmplifier, levitationAmplifier, slowFallingAmplifier, dolphinsGraceAmplifier, slownessAmplifier;
    public int lastSpeedAmplifier;
    public int sinceConsumeTicks = 100, sincePlaceWebTicks = 100, sinceEnderPearlTicks = 100, counterO = 100, sinceDamageTicks = 100;
    public int sincePlaceIceTicks = 100, sinceBlockBreakTicks = 100, flyingTicks, sinceHoglinDamageTicks, sinceAbilitiesTicks;
    public int sinceMythicMobTicks = 500, sinceCancelledMoveTicks = 1000, sinceRavagerDamageTicks = 1000, sinceWitherDamageTicks = 100;
    public int sinceEnderCrystalDamageTicks = 100, counterX = 100, sinceBucketUseTicks = 100, sinceNetherrackBreakTicks = 100;
    public int counterC = 100, sinceServerSlotTicks = 100, sinceBridgingTicks = 100, sinceJumpStrengthChangeTicks, sinceGravityChangeTicks;
    public int ticksNotGlidingElytra, sinceGlidingTicks = 100, sinceSoulPlaceTicks = 100;
    public double lastDigX, lastDigZ, distanceToLastDig, lastBreakX, lastBreakY, lastBreakZ, distanceToLastBreak;
    public int lastScaffoldingX, lastScaffoldingY, lastScaffoldingZ;
    public double distanceToLastScaffolding, glidingDeltaXZ;
    public long lastStartDigMillis, lastFinishDigMillis, lastCancelDigMillis;
    public GameMode packetGameMode = GameMode.SURVIVAL;
    public ItemStack helmet, chestplate, leggings, boots, offhand;

    public ActionTracker(PlayerData data) {
        this.data = data;
    }

    private int tick() {
        return CinderAC.get().tick();
    }

    public void handleEntityAction(WrapperPlayClientEntityAction a) {
        sentEntityActionThisTick = true;
        switch (a.getAction()) {
            case START_SPRINTING:
                sprinting = true;
                break;
            case STOP_SPRINTING:
                sprinting = false;
                break;
            case START_SNEAKING:
                sneaking = true;
                break;
            case STOP_SNEAKING:
                sneaking = false;
                break;
            case START_FLYING_WITH_ELYTRA: {
                Player p = data.player;
                if (p == null || !ServerVersion.atLeast(9)) break;
                if (data.clientVersion().isOlderThan(ClientVersion.V_1_15)) break;
                if (data.position.onGround || data.position.lastOnGround) return;
                ItemStack chest = p.getInventory().getChestplate();
                if (chest != null && chest.getType() == Material.ELYTRA) gliding = true;
                break;
            }
            default:
                break;
        }
    }

    public void handleUpdateAttributes(WrapperPlayServerUpdateAttributes w) {
        data.transactions.onLastTransactionConfirm(() -> {
            for (WrapperPlayServerUpdateAttributes.Property p : w.getProperties()) {
                if (p.getAttribute() == Attributes.STEP_HEIGHT) stepHeightAttribute = attributeValue(p, false);
                if (p.getAttribute() == Attributes.JUMP_STRENGTH) {
                    lastJumpStrength = jumpStrengthAttribute;
                    jumpStrengthAttribute = attributeValue(p, false);
                    if (jumpStrengthAttribute != lastJumpStrength) sinceJumpStrengthChangeTicks = 0;
                }
                if (p.getAttribute() == Attributes.GRAVITY) {
                    lastGravity = gravityAttribute;
                    gravityAttribute = attributeValue(p, false);
                    if (gravityAttribute != lastGravity) sinceGravityChangeTicks = 0;
                }
                if (p.getAttribute() == Attributes.MOVEMENT_SPEED) movementSpeedAttribute = attributeValue(p, true);
                if (p.getAttribute() == Attributes.ENTITY_INTERACTION_RANGE) interactionRange = attributeValue(p, false);
            }
        });
    }

    /** Attribute value with modifiers; for speed without the sprint modifier. */
    static double attributeValue(WrapperPlayServerUpdateAttributes.Property p, boolean withoutSprint) {
        List<WrapperPlayServerUpdateAttributes.PropertyModifier> mods = new ArrayList<>(p.getModifiers());
        if (withoutSprint) {
            mods.removeIf(m -> {
                String id = m.getUUID() == null ? "" : m.getUUID().toString();
                String name = m.getName() == null ? "" : m.getName().toString().toLowerCase();
                return id.equals("662a6b8d-da3e-4c1c-8813-96ea6097278d") || id.equals("19156c30-b575-3f85-a869-c0a29ccaa5fa")
                        || name.contains("sprint");
            });
        }
        double base = p.getValue();
        for (WrapperPlayServerUpdateAttributes.PropertyModifier m : mods) {
            if (m.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION) base += m.getAmount();
        }
        double value = base;
        for (WrapperPlayServerUpdateAttributes.PropertyModifier m : mods) {
            if (m.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE) value += base * m.getAmount();
        }
        for (WrapperPlayServerUpdateAttributes.PropertyModifier m : mods) {
            if (m.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL) value *= 1.0 + m.getAmount();
        }
        return value;
    }

    public void handleInteract(WrapperPlayClientInteractEntity w) {
        if (w.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            digging = false;
            distanceToLastDig = 1000.0;
        }
    }

    public void handleDigging(WrapperPlayClientPlayerDigging d) {
        diggingThisTick = true;
        switch (d.getAction()) {
            case START_DIGGING:
                digging = true;
                lastStartDigMillis = System.currentTimeMillis();
                break;
            case FINISHED_DIGGING:
                lastFinishDigMillis = System.currentTimeMillis();
                lastDigX = d.getBlockPosition().getX();
                lastDigZ = d.getBlockPosition().getZ();
                digging = false;
                break;
            case CANCELLED_DIGGING:
                lastCancelDigMillis = System.currentTimeMillis();
                lastDigX = d.getBlockPosition().getX();
                lastDigZ = d.getBlockPosition().getZ();
                digging = false;
                break;
            case RELEASE_USE_ITEM:
                releasedUseItemThisTick = true;
                break;
            case DROP_ITEM:
            case DROP_ITEM_STACK:
                lastDropTick = tick();
                break;
            default:
                break;
        }
    }

    public void handleDamageByEntity(EntityType type) {
        String n = type.name();
        if (n.equals("ENDER_DRAGON")) sinceDragonDamageTicks = 0;
        if (n.equals("IRON_GOLEM")) sinceIronGolemDamageTicks = 0;
        if (n.equals("FIREBALL") || n.equals("SMALL_FIREBALL") || n.equals("DRAGON_FIREBALL")) sinceFireballDamageTicks = 0;
        if (n.equals("WITHER") || n.equals("WITHER_SKULL")) sinceWitherDamageTicks = 0;
        if (n.equals("ENDER_CRYSTAL") || n.equals("END_CRYSTAL")) sinceEnderCrystalDamageTicks = 0;
        if (n.equals("HOGLIN")) sinceHoglinDamageTicks = 0;
        if (n.equals("RAVAGER")) sinceRavagerDamageTicks = 0;
    }

    public void handleClientStatus(WrapperPlayClientClientStatus s) {
        if (s.getAction() == WrapperPlayClientClientStatus.Action.PERFORM_RESPAWN) lastRespawnTick = tick();
    }

    public void handleBlockPlacePacket() {
        placedThisTick = true;
        data.position.onFireworkHeld(data.player);
    }

    public void handleWindowClick() {
        lastWindowClickTick = tick();
    }

    public void handleAnimation() {
        if (digging) lastDigSwingTick = tick();
        if (lastDigX != 0.0 && lastDigZ != 0.0) {
            distanceToLastDig = VMath.hypot(Math.abs(data.position.x - lastDigX), Math.abs(data.position.z - lastDigZ));
            if (distanceToLastDig > 12.0) {
                distanceToLastDig = 0.0;
                lastDigZ = 0.0;
                lastDigX = 0.0;
            }
        }
    }

    /** BlockPlaceEvent: if the stack and slot did not change, no block was used = the placement was cancelled. */
    public void trackPlacedItem(Player p) {
        placeItemAmount = p == null ? 0 : p.getItemInHand().getAmount();
        placeSlot = p == null ? 0 : p.getInventory().getHeldItemSlot();
        if (placeItemAmount == lastPlaceItemAmount && placeSlot == lastPlaceSlot) sincePlaceTicks = 0;
        lastPlaceSlot = placeSlot;
        lastPlaceItemAmount = placeItemAmount;
    }

    public void onBlockBreak(double x, double y, double z) {
        if (digging) digging = false;
        sinceBlockBreakTicks = 0;
        lastBreakX = x;
        lastBreakY = y;
        lastBreakZ = z;
    }

    public void handleServerPosition(WrapperPlayServerPlayerPositionAndLook w, Runnable afterSend) {
        data.transactions.onLastTransactionConfirm(() -> pendingServerPositions.add(new Vector(w.getX(), w.getY(), w.getZ())));
        data.transactions.onNextTransactionConfirm(pendingServerPositions::poll);
        afterSend.run();
    }

    public void handleEntityEffect(WrapperPlayServerEntityEffect w) {
        data.transactions.onLastTransactionConfirm(() -> {
            int amp = w.getEffectAmplifier() + 1;
            PotionType t = w.getPotionType();
            if (t == PotionTypes.SPEED) {
                hasSpeed = true;
                lastSpeedAmplifier = speedAmplifier = amp;
            }
            if (t == PotionTypes.HASTE) hasHaste = true;
            if (t == PotionTypes.SLOWNESS) {
                hasSlowness = true;
                slownessAmplifier = amp;
            }
            if (t == PotionTypes.JUMP_BOOST) {
                hasJumpBoost = true;
                jumpBoostAmplifier = amp;
            }
            if (t == PotionTypes.LEVITATION) {
                hasLevitation = true;
                levitationAmplifier = amp;
            }
            if (t == PotionTypes.SLOW_FALLING) {
                hasSlowFalling = true;
                slowFallingAmplifier = amp;
            }
            if (t == PotionTypes.CONDUIT_POWER) hasConduitPower = true;
            if (t == PotionTypes.DOLPHINS_GRACE) {
                hasDolphinsGrace = true;
                dolphinsGraceAmplifier = amp;
            }
        });
    }

    public void handleRemoveEntityEffect(WrapperPlayServerRemoveEntityEffect w) {
        data.transactions.onLastTransactionConfirm(() -> {
            PotionType t = w.getPotionType();
            if (t == PotionTypes.SPEED) {
                speedAmplifier = 0;
                hasSpeed = false;
                data.position.sinceSpeedTicks = 0;
            }
            if (t == PotionTypes.SLOWNESS) {
                hasSlowness = false;
                slownessAmplifier = 0;
            }
            if (t == PotionTypes.HASTE) hasHaste = false;
            if (t == PotionTypes.JUMP_BOOST) {
                jumpBoostAmplifier = 0;
                hasJumpBoost = false;
            }
            if (t == PotionTypes.LEVITATION) {
                levitationAmplifier = 0;
                hasLevitation = false;
            }
            if (t == PotionTypes.SLOW_FALLING) {
                hasSlowFalling = false;
                slowFallingAmplifier = 0;
            }
            if (t == PotionTypes.CONDUIT_POWER) hasConduitPower = false;
            if (t == PotionTypes.DOLPHINS_GRACE) {
                dolphinsGraceAmplifier = 0;
                hasDolphinsGrace = false;
            }
        });
    }

    public void handleGameState(WrapperPlayServerChangeGameState w) {
        data.transactions.onLastTransactionConfirm(() -> {
            if (w.getReason() == WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE) {
                switch ((int) w.getValue()) {
                    case 0:
                        packetGameMode = GameMode.SURVIVAL;
                        break;
                    case 1:
                        packetGameMode = GameMode.CREATIVE;
                        break;
                    case 2:
                        packetGameMode = GameMode.ADVENTURE;
                        break;
                    case 3:
                        packetGameMode = GameMode.SPECTATOR;
                        break;
                    default:
                        break;
                }
            }
            if (w.getReason() == WrapperPlayServerChangeGameState.Reason.BEGIN_RAINING) raining = true;
            if (w.getReason() == WrapperPlayServerChangeGameState.Reason.END_RAINING) raining = false;
        });
    }

    public void handleMetadata(WrapperPlayServerEntityMetadata w) {
        data.transactions.onLastTransactionConfirm(() -> {
            EntityData<?> flags = find(w.getEntityMetadata(), 0);
            if (flags != null && flags.getValue() instanceof Byte) {
                byte b = (Byte) flags.getValue();
                gliding = (b & 0x80) == 128 && data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
                swimming = (b & 0x10) == 16;
            }
            if (ServerVersion.atLeast(13, 1) && data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                EntityData<?> hand = find(w.getEntityMetadata(), ServerVersion.atLeast(17) ? 8 : 7);
                if (hand != null && hand.getValue() instanceof Byte) riptiding = ((Byte) hand.getValue() & 4) == 4;
            }
        });
    }

    private static EntityData<?> find(List<EntityData<?>> list, int index) {
        for (EntityData<?> d : list) if (d.getIndex() == index) return d;
        return null;
    }

    public void handleDamage(EntityDamageEvent.DamageCause cause) {
        if (cause == null) return;
        lastDamageTick = tick();
        sinceDamageTicks = 0;
        String n = cause.name();
        if (!n.equals("FALL") && !n.equals("SUFFOCATION")) sinceNonFallDamageTicks = 0;
        switch (n) {
            case "ENTITY_ATTACK":
                sinceAttackDamageTicks = 0;
                break;
            case "FIRE":
            case "FIRE_TICK":
                sinceFireDamageTicks = 0;
                break;
            case "FALL":
                sinceFallDamageTicks = 0;
                break;
            case "MAGIC":
                sinceMagicDamageTicks = 0;
                break;
            case "POISON":
                sincePoisonDamageTicks = 0;
                break;
            case "CONTACT":
                sinceContactDamageTicks = 0;
                break;
            case "PROJECTILE":
                sinceProjectileDamageTicks = 0;
                break;
            case "LAVA":
                sinceLavaDamageTicks = 0;
                break;
            default:
                break;
        }
    }

    public void handleFlying(WrapperPlayClientPlayerFlying f) {
        acceptedServerPosition = false;
        if (f.hasPositionChanged() && f.hasRotationChanged() && !f.isOnGround()) {
            for (Vector v : pendingServerPositions) {
                // signed differences, not absolute - exactly how the original computes it
                double dx = v.getX() - f.getLocation().getX();
                double dy = v.getY() - f.getLocation().getY();
                double dz = v.getZ() - f.getLocation().getZ();
                if (dx < 1.0E-8 && dy < 1.0E-8 && dz < 1.0E-8) acceptedServerPosition = true;
            }
        }
        if (acceptedServerPosition) {
            PositionSnapshot now = data.position.snapshot(0), prev = data.position.snapshot(1);
            now.deltaX = now.deltaY = now.deltaZ = now.deltaXZ = 0.0;
            now.teleport = true;
            now.onGround = prev.onGround;
        }
        sinceServerPositionTicks = acceptedServerPosition ? 0 : sinceServerPositionTicks + 1;
        releasedUseItemThisTick = false;
        diggingThisTick = false;
        sentEntityActionThisTick = false;
        placedThisTick = false;
        ++sinceSoulPlaceTicks;
        ++sinceGravityChangeTicks;
        ++sinceJumpStrengthChangeTicks;
        ++flyingTicks;
        ++sinceFireDamageTicks;
        ++sinceMagicDamageTicks;
        ++sinceFallDamageTicks;
        ++sinceAttackDamageTicks;
        ++sinceNonFallDamageTicks;
        ++sinceContactDamageTicks;
        ++sinceWitherDamageTicks;
        ++sincePoisonDamageTicks;
        ++counterX;
        ++sinceProjectileDamageTicks;
        ++sinceLavaDamageTicks;
        ++sinceDragonDamageTicks;
        ++sinceHoglinDamageTicks;
        ++sinceBucketUseTicks;
        ++sinceMythicMobTicks;
        ++sinceBucketTicks;
        ++sincePlaceBlockTicks;
        ++sinceIronGolemDamageTicks;
        ++sinceGlassBottleTicks;
        ++sincePlaceClimbableTicks;
        ++sinceServerSlotTicks;
        ++sincePlaceSlimeTicks;
        ++sinceEnderCrystalDamageTicks;
        ++sinceWorldChangeTicks;
        ++sinceTotemTicks;
        ++sinceTeleportTicks;
        ++sinceDeathTicks;
        ++counterF;
        ++counterT;
        ++sinceFireballDamageTicks;
        ++sincePlaceWebTicks;
        ++counterO;
        ++sinceConsumeTicks;
        ++counterC;
        ++sinceEnderPearlTicks;
        ++sincePlaceTicks;
        ++sinceAbilitiesTicks;
        ++sinceDamageTicks;
        ++sincePlaceIceTicks;
        ++sinceBlockBreakTicks;
        ++sinceRavagerDamageTicks;
        ++sinceNetherrackBreakTicks;
        sinceBridgingTicks = bridging ? 0 : sinceBridgingTicks + 1;
        if (!f.hasPositionChanged()) return;
        ++positionTicks;
        Player p = data.player;
        if (p != null) {
            helmet = p.getInventory().getHelmet();
            chestplate = p.getInventory().getChestplate();
            leggings = p.getInventory().getLeggings();
            boots = p.getInventory().getBoots();
            if (ServerVersion.atLeast(9)) offhand = p.getInventory().getItemInOffHand();
            depthStrider = boots != null && hasEnchant(boots, "DEPTH_STRIDER");
            aquaAffinity = helmet != null && (hasEnchant(helmet, "WATER_WORKER") || hasEnchant(helmet, "AQUA_AFFINITY"));
            swiftSneak = ServerVersion.atLeast(19) && leggings != null && hasEnchant(leggings, "SWIFT_SNEAK");
            wearingElytra = ServerVersion.atLeast(9) && chestplate != null && chestplate.getType().name().equals("ELYTRA");
            glidingBukkit = wearingElytra && p.isGliding();
        }
        sinceGlidingTicks = gliding ? 0 : sinceGlidingTicks + 1;
        if (glidingBukkit) glidingDeltaXZ = data.position.deltaXZ;
        ticksNotGlidingElytra = glidingBukkit ? 0 : ticksNotGlidingElytra + 1;
        double x = f.getLocation().getX(), y = f.getLocation().getY(), z = f.getLocation().getZ();
        sprintingTicks = sprinting ? sprintingTicks + 1 : 0;
        ticksNotSprinting = !sprinting ? ticksNotSprinting + 1 : 0;
        if (lastBreakX != 0.0 && lastBreakZ != 0.0) {
            distanceToLastBreak = VMath.magnitude(Math.abs(x - lastBreakX), Math.abs(y - lastBreakY), Math.abs(z - lastBreakZ));
            if (distanceToLastBreak > 8.0) {
                distanceToLastBreak = 0.0;
                lastBreakZ = 0.0;
                lastBreakX = 0.0;
            }
        }
        if (lastScaffoldingX != 0 && lastScaffoldingY != 0 && lastScaffoldingZ != 0) {
            distanceToLastScaffolding = VMath.magnitude(Math.abs(x - lastScaffoldingX), Math.abs(y - lastScaffoldingY),
                    Math.abs(z - lastScaffoldingZ));
            if (distanceToLastScaffolding > 12.0) lastScaffoldingZ = lastScaffoldingY = lastScaffoldingX = 0;
        }
    }

    private static boolean hasEnchant(ItemStack item, String name) {
        Enchantment e = Enchantment.getByName(name);
        return e != null && item.containsEnchantment(e);
    }
}
