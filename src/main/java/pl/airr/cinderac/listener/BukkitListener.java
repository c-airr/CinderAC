package pl.airr.cinderac.listener;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;
import pl.airr.cinderac.util.VMath;

import java.util.ArrayList;
import java.util.List;

/** Bukkit events available since 1.8 (player owner thread). */
public final class BukkitListener implements Listener {
    private final CinderAC plugin;

    public BukkitListener(CinderAC plugin) {
        this.plugin = plugin;
    }

    private PlayerData data(Player p) {
        return plugin.players().get(p);
    }

    /** 3x4x3 grid around the feet (x,z -1..1; y -1..2), order x -> y -> z; unloaded chunk = SPONGE. */
    static List<Material> scan(World world, int bx, int by, int bz) {
        List<Material> out = new ArrayList<>(36);
        if (world == null) return out;
        for (int x = bx - 1; x <= bx + 1; x++) {
            for (int y = by - 1; y <= by + 2; y++) {
                for (int z = bz - 1; z <= bz + 1; z++) {
                    out.add(world.isChunkLoaded(x >> 4, z >> 4) ? world.getBlockAt(x, y, z).getType() : Material.SPONGE);
                }
            }
        }
        return out;
    }

    /** Ring of 9 blocks at y-2. */
    static List<Material> ringBelow(World world, int x, int y, int z) {
        List<Material> out = new ArrayList<>(9);
        if (world == null) return out;
        int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {-1, -1}, {0, 0}, {1, -1}};
        for (int[] o : d) out.add(world.getBlockAt(x + o[0], y - 2, z + o[1]).getType());
        return out;
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Settings s = Settings.get();
        if (p.hasPermission("cinderac.alerts") && s.toggleAlertsOnJoin) plugin.alerts().enable(p);
        PlayerData d = data(p);
        if (d == null) return;
        synchronized (d) {
            d.player = p;
            Location l = p.getLocation();
            d.position.setbackX = l.getX();
            d.position.setbackY = l.getY();
            d.position.setbackZ = l.getZ();
            d.position.joinX = l.getX();
            d.position.joinY = l.getY();
            d.position.joinZ = l.getZ();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.alerts().remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom(), to = e.getTo();
        if (to == null || from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null) return;
        World w = to.getWorld();
        int bx = floor(to.getX()), by = floor(to.getY()), bz = floor(to.getZ());
        List<Material> near = scan(w, bx, by, bz);
        List<Material> ring = ringBelow(w, bx, by, bz);
        Material b1 = w.getBlockAt(bx, floor(to.getY() - 1.0), bz).getType();
        Material b2 = w.getBlockAt(bx, floor(to.getY() - 2.0), bz).getType();
        Material b3 = w.getBlockAt(bx, floor(to.getY() - 3.0), bz).getType();
        boolean waterlogged = ServerVersion.atLeast(13, 1) && (waterlogged(w.getBlockAt(bx, by - 1, bz)) || waterlogged(w.getBlockAt(bx, by, bz))
                || waterlogged(w.getBlockAt(bx, by + 1, bz)) || waterlogged(w.getBlockAt(bx, by + 2, bz)));
        List<Entity> entities = p.getNearbyEntities(1.5, 2.0, 1.5);
        synchronized (d) {
            if (e.isCancelled()) d.action.sinceCancelledMoveTicks = 0;
            if (ServerVersion.atLeast(9) && p.isGliding()) d.position.sinceGlidingTicks = 0;
            if (ServerVersion.atLeast(9) && d.clientVersion().isOlderThanOrEquals(ClientVersion.V_1_9) && d.action.pendingGlideFix) {
                d.action.pendingGlideFix = false;
                p.setGliding(true);
            }
            if (ServerVersion.atLeast(13, 1) && d.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_13) && d.action.pendingSwimFix) {
                d.action.pendingSwimFix = false;
                p.setSwimming(true);
            }
            d.position.nearMaterials = near;
            d.position.ringBelow2 = ring;
            d.position.below1 = b1;
            d.position.below2 = b2;
            d.position.below3 = b3;
            d.position.nearEntities = entities;
            if (waterlogged) d.position.onWaterlogged();
            if (p.getVehicle() != null) d.position.vehicleMaterials = near;
        }
        if (ServerVersion.isFolia()) d.handleBukkit(e);
    }

    private static boolean waterlogged(Block b) {
        return b.getBlockData() instanceof org.bukkit.block.data.Waterlogged && ((org.bukkit.block.data.Waterlogged) b.getBlockData()).isWaterlogged();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null || e.getTo() == null) return;
        Location to = e.getTo();
        List<Material> near = null;
        if (ServerVersion.atLeast(13, 1) && to.getWorld() != null && to.getWorld().isChunkLoaded(floor(to.getX()) >> 4, floor(to.getZ()) >> 4)) {
            near = scan(to.getWorld(), floor(to.getX()), floor(to.getY()), floor(to.getZ()));
        }
        synchronized (d) {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                d.action.sinceEnderPearlTicks = 0;
                if (d.frozen) e.setCancelled(true);
            }
            if (e.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN && e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                d.action.sinceTeleportTicks = 0;
            }
            if (near != null) {
                d.position.nearMaterials = near;
                d.position.allAirTicks = 0;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.action.sinceWorldChangeTicks = 0;
            d.position.allAirTicks = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof Firework)) return;
        Object shooter = ServerVersion.atLeast(16) ? ((Firework) e.getEntity()).getShooter() : null;
        if (!(shooter instanceof Player)) return;
        PlayerData d = data((Player) shooter);
        if (d == null) return;
        synchronized (d) {
            d.position.sinceFireworkTicks = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null) return;
        Material type = e.getBlock().getType();
        boolean bridging = bridging(e, d);
        synchronized (d) {
            d.action.trackPlacedItem(p);
            if (Blocks.SLIME.is(type)) d.action.sincePlaceSlimeTicks = 0;
            if (e.isCancelled()) d.action.sincePlaceTicks = 0;
            if (Blocks.ICE.is(type)) d.action.sincePlaceIceTicks = 0;
            if (Blocks.CLIMBABLE.is(type)) d.action.sincePlaceClimbableTicks = 0;
            if (Blocks.WEB.is(type)) d.action.sincePlaceWebTicks = 0;
            if (d.frozen) {
                if (p.getVehicle() != null) p.leaveVehicle();
                e.setCancelled(true);
            }
            if (Blocks.SCAFFOLDING.is(type)) {
                d.action.lastScaffoldingX = e.getBlockPlaced().getX();
                d.action.lastScaffoldingY = e.getBlockPlaced().getY();
                d.action.lastScaffoldingZ = e.getBlockPlaced().getZ();
            }
            if (Blocks.SOUL.is(type)) d.action.sinceSoulPlaceTicks = 0;
            d.action.sincePlaceBlockTicks = 0;
            d.action.bridging = bridging;
        }
        d.handleBukkit(e);
    }

    /** Placing a full block against a side, in the air below the player (bridging). */
    private static boolean bridging(BlockPlaceEvent e, PlayerData d) {
        Material type = e.getBlock().getType();
        if (!type.isSolid() || Blocks.PATH.is(type) || Blocks.FARMLAND.is(type) || Blocks.SAND.is(type)
                || Blocks.CONCRETE_POWDER.is(type) || Blocks.GRAVEL.is(type)) {
            return false;
        }
        Material below2 = d.position.below2;
        org.bukkit.block.BlockFace face = e.getBlockAgainst().getFace(e.getBlock());
        double dist = VMath.hypot(Math.abs(e.getBlockAgainst().getX() - d.position.x), Math.abs(e.getBlockAgainst().getZ() - d.position.z));
        List<Material> below = d.position.blocksBelow;
        if (below == null) return false;
        int air = 0;
        for (Material m : below) if (Blocks.isAir(m)) air++;
        return below2 != null && face != null && face != org.bukkit.block.BlockFace.UP && Blocks.isAir(below2)
                && dist < 2.25 && e.getBlockAgainst().getY() < d.position.y && air > 6;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null) return;
        synchronized (d) {
            d.action.onBlockBreak(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
            if (e.isCancelled()) d.action.lastCancelledBreakTick = plugin.tick();
            if (d.frozen) e.setCancelled(true);
            if (e.getBlock().getType().name().equals("NETHERRACK")) d.action.sinceNetherrackBreakTicks = 0;
        }
        d.handleBukkit(e);
        synchronized (d) {
            d.action.instaBreak = false;
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null || !e.getInstaBreak()) return;
        synchronized (d) {
            d.action.instaBreak = true;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerPickupItemEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.action.lastPickupTick = plugin.tick();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConsume(PlayerItemConsumeEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null || !e.getItem().toString().contains("CHORUS")) return;
        synchronized (d) {
            d.action.sinceConsumeTicks = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedEnter(PlayerBedEnterEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.action.lastBedEnterTick = plugin.tick();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedLeave(PlayerBedLeaveEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.action.lastBedLeaveTick = plugin.tick();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) d.handleBukkit(e);
        synchronized (d) {
            boolean right = e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK;
            if (right && e.getItem() != null && Blocks.GLASS_BOTTLE.is(e.getItem().getType())) d.action.sinceGlassBottleTicks = 0;
            if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && Blocks.NOTE_BLOCK.is(e.getClickedBlock().getType())) {
                d.clicks.lastNoteBlockInteract = System.currentTimeMillis();
            }
            if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType().name().contains("BUCKET")) {
                    d.action.sinceBucketUseTicks = 0;
                }
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType().name().contains("_BUCKET")
                        && e.getClickedBlock() != null && !Blocks.isAir(e.getClickedBlock().getType())) {
                    Block b = e.getClickedBlock();
                    Location l = p.getLocation();
                    double dist = VMath.magnitude(Math.abs(b.getX() - l.getX()), Math.abs(b.getY() - l.getY()), Math.abs(b.getZ() - l.getZ()));
                    if (dist < 2.25) d.action.sinceBucketTicks = 0;
                }
                d.action.lastInteractTick = plugin.tick();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        PlayerData d = data(e.getEntity());
        if (d == null) return;
        synchronized (d) {
            d.action.sinceDeathTicks = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player) || e.isCancelled()) return;
        PlayerData d = data((Player) e.getEntity());
        if (d == null) return;
        if (d.frozen) e.setCancelled(true);
        synchronized (d) {
            d.action.handleDamage(e.getCause());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        String type = e.getRightClicked().getType().name();
        synchronized (d) {
            switch (type) {
                case "VILLAGER":
                case "COW":
                case "CHICKEN":
                case "WOLF":
                    d.clicks.lastAnimalInteract = System.currentTimeMillis();
                    // as in the original - falls through to the item frame case
                case "ITEM_FRAME":
                    d.clicks.lastItemFrameInteract = System.currentTimeMillis();
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLaunch(ProjectileLaunchEvent e) {
        org.bukkit.entity.Projectile proj = e.getEntity();
        Object shooter = proj.getShooter();
        if (!(shooter instanceof Player)) return;
        PlayerData d = data((Player) shooter);
        if (d == null) return;
        if (proj instanceof Arrow) {
            d.handleBukkit(e);
        } else if (proj instanceof Snowball || proj instanceof Egg || proj instanceof ThrownExpBottle) {
            synchronized (d) {
                d.action.lastProjectileLaunchTick = plugin.tick();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent e) {
        if (e.getCaught() instanceof Player) {
            PlayerData caught = data((Player) e.getCaught());
            if (caught != null) {
                synchronized (caught) {
                    caught.position.onFished();
                }
            }
        } else if (e.getCaught() instanceof Boat) {
            plugin.markFishedBoat(e.getCaught().getEntityId());
        }
        PlayerData d = data(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.action.lastFishTick = plugin.tick();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        Settings s = Settings.get();
        if (e.getDamager() instanceof Player) {
            PlayerData attacker = data((Player) e.getDamager());
            if (attacker != null && System.currentTimeMillis() - attacker.lastFlagTime < s.cancelHitsDuration) {
                e.setDamage(0.0);
                if (s.cancelHitsEvent) e.setCancelled(true);
            }
        }
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            if (victim.hasMetadata("NPC") || victim.hasMetadata("npc")) return;
            PlayerData d = data(victim);
            if (d == null) return;
            synchronized (d) {
                d.action.handleDamageByEntity(e.getDamager().getType());
            }
        }
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player) || e.isCancelled()) return;
        PlayerData attacker = data((Player) e.getDamager());
        PlayerData victim = data((Player) e.getEntity());
        if (attacker == null || victim == null) return;
        if (victim.frozen || attacker.frozen) e.setCancelled(true);
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        synchronized (attacker) {
            attacker.combat.onAttackEvent();
        }
        synchronized (victim) {
            victim.combat.onDamagedEvent();
        }
    }
}
