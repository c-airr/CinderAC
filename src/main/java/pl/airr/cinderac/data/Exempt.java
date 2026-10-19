package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;

import java.util.function.Predicate;

/** Situations in which checks stand down (lag, surroundings, effects, recent events). */
public enum Exempt {
    TPS(d -> CinderAC.get().tps() < 19.25),
    CHUNK(d -> d.player != null && !d.player.getWorld().isChunkLoaded(floor(d.position.x) >> 4, floor(d.position.z) >> 4)),
    JOINED(d -> System.currentTimeMillis() - d.joinTime < Settings.get().joinCheckWaitTime || d.action.positionTicks < Settings.get().minTicksExisted),
    CAKE(d -> d.position.nearCake),
    BAMBOO(d -> d.position.nearBamboo),
    HAPPY_GHAST(d -> d.position.nearHappyGhast || d.position.sinceHappyGhastTicks < 25),
    PACKET_EXPLOSION(d -> d.velocity.sinceExplosionTicks < 16),
    ENTITY_CRAM_FIX(d -> Settings.get().entityCramFix && d.position.sinceEntityCramTicks < Settings.get().entityCramExemptTicks),
    AROUND_SLIME(d -> d.position.sinceSlimeAroundTicks < 20),
    SIGN(d -> d.position.nearSign),
    PRESSURE_PLATE(d -> d.position.nearPressurePlate),
    SWIMMING(d -> ServerVersion.atLeast(9) && d.position.sinceSwimmingTicks < 40),
    SWIMMING_JESUS(d -> ServerVersion.atLeast(9) && d.position.sinceSwimmingTicks < 5),
    COLLIDING_HORIZONTALLY(d -> d.position.blocksAroundNotAir),
    DRIPSTONE(d -> d.position.nearDripstone),
    HIGH_FLY_SPEED(d -> d.position.sinceHighFlySpeedTicks < 100),
    GLITCHED_BLOCKS_ABOVE(d -> d.position.blocksAtHead != null && !Blocks.all(d.position.blocksAtHead, Blocks.AIR)),
    PROJECTILE_DAMAGE(d -> d.action.sinceProjectileDamageTicks < 20),
    CHEST(d -> d.position.nearChest),
    ATTACK_DAMAGE(d -> d.action.sinceAttackDamageTicks < 80),
    SKULL(d -> d.position.nearSkull),
    EXPLOSION(d -> d.velocity.sinceExplosionTicks < 50),
    STAIRS(d -> d.position.nearStairs),
    HIGH_LEVITATION(d -> ServerVersion.atLeast(9) && d.action.levitationAmplifier > 10),
    FENCE_GATE(d -> d.position.nearGate),
    POWDER_SNOW(d -> d.position.nearPowderSnow),
    DRAGON_DAMAGE(d -> d.action.sinceDragonDamageTicks < 60),
    JUMP_BOOST_RAN_OUT(d -> d.position.sinceJumpBoostTicks > 0 && d.position.sinceJumpBoostTicks < 30),
    LAGGED_NEAR_GROUND(d -> (d.position.nearGround() || !d.position.airBelow()) && d.keepAlive.fastFlying),
    SNOW(d -> d.position.nearSnow),
    NETHERITE_ARMOR(Exempt::netheriteArmor),
    FAST_ZERO(d -> d.position.deltaY == 0.0 && d.keepAlive.fastFlying),
    JOINED_CHUNK_LOAD(d -> System.currentTimeMillis() - d.joinTime < 30000L && d.position.deltaY < (double) -0.05f && d.position.deltaY > (double) -0.1f),
    NEAR_SOLID(d -> d.position.solidNear),
    CAMPFIRE(d -> d.position.nearCampfire),
    CANCELLED_MOVE(d -> d.action.sinceCancelledMoveTicks < 5),
    CARPET(d -> d.position.nearCarpet),
    FLOWER_POT(d -> d.position.nearFlowerPot),
    EMPTIED_BUCKET(d -> d.action.sinceBucketTicks < 15),
    END_ROD(d -> d.position.nearEndRod),
    HOPPER(d -> d.position.nearHopper),
    CHAIN(d -> d.position.nearChain),
    DOOR(d -> d.position.nearDoor),
    PICKED_UP_ITEM(d -> tick() - d.action.lastPickupTick < 30),
    ANVIL(d -> d.position.nearAnvil),
    LAGGED_NEAR_GROUND_MODERN(d -> ServerVersion.atLeast(13, 1) && d.keepAlive.flyingDelayMillis < 10L
            && (d.position.nearGroundModern() || !d.position.airBelow())),
    FULLY_SUBMERGED(d -> d.position.fullySubmerged),
    RESPAWN(d -> tick() - d.action.lastRespawnTick < 20),
    ICE(d -> d.position.sinceIceTicks < 20 || d.position.nearIce),
    BED(d -> d.position.nearBed || d.position.sinceBedTicks < 30),
    SLAB(d -> d.position.nearSlab),
    HIGH_JUMP_BOOST(d -> d.action.jumpBoostAmplifier > 2),
    LILY_PAD(d -> d.position.nearLilyPad),
    HIGH_SPEED(d -> d.action.speedAmplifier > 5),
    WALL(d -> d.position.nearWall),
    SWEET_BERRIES(d -> d.position.nearBerryBush),
    CONDUIT(d -> d.position.nearConduit),
    PLACED_WEB(d -> d.action.sincePlaceWebTicks < 25),
    PISTON(d -> d.position.nearPiston || d.position.sincePistonTicks < 20),
    CHORUS_FRUIT(d -> d.action.sinceConsumeTicks < 25),
    ENDER_PEARL(d -> d.action.sinceEnderPearlTicks < 5),
    NEAR_GROUND(d -> d.position.nearGround()),
    SLEEPING(d -> d.player != null && d.player.isSleeping() || tick() - d.action.lastBedEnterTick < 40 || tick() - d.action.lastBedLeaveTick < 40),
    SOUL_SPEED(d -> ServerVersion.atLeast(16) && d.position.sinceSoulSpeedTicks < 60),
    TRAPDOOR(d -> d.position.sinceTrapdoorTicks < 20 || d.position.nearTrapdoor),
    FISHING_ROD(d -> ServerVersion.atLeast(9) && d.position.sinceFishedTicks < 100),
    PLUGIN_LOAD(d -> System.currentTimeMillis() - CinderAC.get().enableTime() < 10000L),
    RIPTIDE(d -> ServerVersion.atLeast(13, 1) && d.position.sinceRiptideTicks < 80),
    FIREWORK(d -> ServerVersion.atLeast(9) && d.position.sinceFireworkTicks < 150),
    SPECTATOR(d -> d.player != null && d.player.getGameMode() == GameMode.SPECTATOR),
    CAULDRON(d -> d.position.nearCauldron),
    ENTITY_COLLISION(d -> Settings.get().entityCollision && d.position.sinceNearLivingEntityTicks < 8),
    DEATH(d -> d.action.sinceDeathTicks < 25),
    DEPTH_STRIDER(d -> d.action.depthStrider && d.position.sinceLiquidTicks < 30),
    LEVITATION(d -> ServerVersion.atLeast(9) && d.position.sinceLevitationTicks < 75),
    FROZEN(d -> d.frozen),
    DIGGING(d -> d.action.digging || tick() - d.action.lastDigSwingTick < 20 || tick() - d.action.lastInteractTick < 20),
    BLOCK_BREAK(d -> d.action.sinceBlockBreakTicks < 25),
    PLACING(d -> d.action.sincePlaceBlockTicks < 10),
    SERVER_POSITION(d -> d.action.sinceServerPositionTicks < 8),
    SERVER_POSITION_FAST(d -> d.action.sinceServerPositionTicks < 5),
    CANCELLED_PLACE(d -> d.action.sincePlaceTicks < 40),
    BLOCK_PLACE(d -> d.action.sincePlaceBlockTicks < 30),
    BLOCK_PLACE_FAST(d -> d.action.sincePlaceBlockTicks < 15),
    SWIMMING_ON_OLD_VERSION(d -> d.position.sinceLiquidTicks < 60 && d.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)
            && !ServerVersion.atLeast(13)),
    FULLY_STUCK(d -> d.position.insideBlock),
    PLACED_CLIMBABLE(d -> d.action.sincePlaceClimbableTicks < 30),
    PARTIALLY_STUCK(d -> d.position.insideBlock2),
    NOT_MOVING(d -> !d.position.moved),
    PLACED_SLIME(d -> d.action.sincePlaceSlimeTicks < 120),
    FIREBALL(d -> d.action.sinceFireballDamageTicks < 25),
    WORLD_CHANGE(d -> d.action.sinceWorldChangeTicks < 40),
    FALL_DAMAGE(d -> d.action.sinceFallDamageTicks < 40),
    ILLEGAL_BLOCK(d -> d.position.nearDaylightDetector || d.position.nearBrewingStand || d.position.nearCarpet || d.position.nearWall
            || d.position.nearSkull || d.position.nearEndPortalFrame || d.position.nearScaffolding || d.position.nearCampfire),
    HONEY(d -> ServerVersion.atLeast(9) && (d.position.nearHoney || d.position.sinceHoneyTicks < 30)),
    SCAFFOLDING(d -> ServerVersion.atLeast(9) && (d.position.nearScaffolding || d.position.sinceScaffoldingTicks < 30)),
    HALF_BLOCK(d -> d.position.nearStairs || d.position.nearSlab),
    SHULKER(d -> d.position.nearShulkerEntity || d.position.sinceShulkerEntityTicks < 40),
    GLIDING(d -> ServerVersion.atLeast(9) && d.action.sinceGlidingTicks < 10),
    ELYTRA(d -> d.action.wearingElytra || d.position.sinceWearingElytraTicks < 100),
    VELOCITY(d -> d.velocity.sinceVelocityTicks < 40),
    DEAD(d -> d.player != null && d.player.isDead()),
    WEB(d -> d.position.sinceWebTicks < 25 && d.position.deltaY < 0.1),
    SOUL_SAND(d -> d.position.nearSoulSand),
    CINEMATIC(d -> Settings.get().cinematic && d.rotation.cinematic),
    FAST(d -> d.keepAlive.fastFlying),
    LENIENT_SCAFFOLDING(d -> Settings.get().lenientScaffolding && ServerVersion.atLeast(9)
            && d.action.distanceToLastScaffolding < 3.0 && d.action.distanceToLastScaffolding > 0.0),
    WINDOW_CLICK(d -> tick() - d.action.lastWindowClickTick < 15),
    DROPPED_ITEM(d -> tick() - d.action.lastDropTick < 15),
    CANCELLED_BREAK(d -> tick() - d.action.lastCancelledBreakTick < 40),
    MYTHIC_MOB(d -> d.action.sinceMythicMobTicks < 50),
    CREATIVE(d -> d.player != null && d.player.getGameMode() == GameMode.CREATIVE || d.action.packetGameMode == GameMode.CREATIVE),
    AUTOCLICKER_NON_DIG(d -> d.exempt(PLACING, DIGGING, BLOCK_BREAK, FAST, DROPPED_ITEM, WINDOW_CLICK, CANCELLED_BREAK, CREATIVE)
            || d.action.sinceGlassBottleTicks < 15 || tick() - d.combat.lastItemFrameTick < 20
            || tick() - d.action.lastProjectileLaunchTick < 20),
    AUTOCLICKER(Exempt::autoclicker),
    COLLIDING_VERTICALLY(d -> d.position.sinceBlockAboveTicks < 20),
    SLOW_FALLING(d -> ServerVersion.atLeast(9) && d.position.sinceSlowFallingTicks < 40),
    BUBBLE_COLUMN(d -> ServerVersion.atLeast(9) && d.position.sinceBubbleColumnTicks < 40),
    FENCE(d -> d.position.nearFence || d.position.sinceFenceTicks < 7),
    JUMP_BOOST(d -> d.position.sinceJumpBoostTicks < 40),
    FLIGHT(d -> d.player != null && d.player.getAllowFlight() || d.position.sinceFlyingTicks < Settings.get().flightCooldown),
    COMBO_MODE(d -> d.action.sinceAttackDamageTicks < 25 && d.player != null && d.player.getMaximumNoDamageTicks() < 15),
    DOLPHINS_GRACE(d -> ServerVersion.atLeast(9) && (d.position.sinceDolphinsGraceTicks < 50 || PositionTracker.bukkitDolphinsGrace(d.player))),
    TELEPORT(d -> d.action.sinceTeleportTicks < 10 || d.action.acceptedServerPosition),
    SERVER_VERSION(d -> ServerVersion.atLeast(9)),
    KELP(d -> d.position.nearKelp),
    CLIENT_VERSION(d -> d.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)),
    SLIME(d -> d.position.nearSlime || d.position.sinceSlimeTicks < 65),
    BOAT(d -> d.position.nearBoat),
    SEA_PICKLE(d -> d.position.nearSeaPickle),
    SEAGRASS(d -> d.position.nearSeagrass),
    TURTLE_EGG(d -> d.position.nearTurtleEgg),
    SHULKER_BOX(d -> d.position.nearShulker || d.position.sinceShulkerTicks < 60),
    WATERLOGGED(d -> ServerVersion.atLeast(13, 1) && d.position.sinceWaterloggedTicks < 30),
    CLIMBABLE(d -> d.position.climbableAtBody || d.position.nearClimbable),
    LIQUID(d -> d.position.sinceLiquidTicks < 25),
    GLASS_PANE(d -> d.position.nearPane),
    FARMLAND(d -> d.position.sinceFarmlandTicks < 20),
    VEHICLE(d -> d.position.sinceVehicleTicks < 25),
    VOID(d -> ServerVersion.atLeast(18) ? d.position.y < -70.0 : d.position.y < 0.0);

    private final Predicate<PlayerData> test;

    Exempt(Predicate<PlayerData> test) {
        this.test = test;
    }

    public boolean test(PlayerData d) {
        return test.test(d);
    }

    private static int tick() {
        return CinderAC.get().tick();
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }

    private static boolean autoclicker(PlayerData d) {
        long now = System.currentTimeMillis();
        ActionTracker a = d.action;
        return d.exempt(PLACING, DIGGING, BLOCK_BREAK, FAST, DROPPED_ITEM, WINDOW_CLICK, CREATIVE, CANCELLED_BREAK)
                || now - a.lastStartDigMillis < 110L || d.position.sinceFireworkTicks < 20
                || now - d.clicks.lastNoteBlockInteract < 3000L || now - d.clicks.lastAnimalInteract < 250L
                || a.sinceGlassBottleTicks < 15 || now - a.lastCancelDigMillis < 110L || now - a.lastFinishDigMillis < 110L
                || tick() - a.lastFishTick < 20 || tick() - a.lastProjectileLaunchTick < 20 || tick() - d.combat.lastItemFrameTick < 20
                || a.distanceToLastDig < 6.5 && a.distanceToLastDig > 0.0;
    }

    /** The first non-empty armor piece decides (as in the original). */
    private static boolean netheriteArmor(PlayerData d) {
        Player p = d.player;
        if (!ServerVersion.atLeast(16) || p == null) return false;
        ItemStack[] parts = {p.getInventory().getHelmet(), p.getInventory().getChestplate(), p.getInventory().getLeggings(), p.getInventory().getBoots()};
        String[] names = {"NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS"};
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] != null) {
                Material t = parts[i].getType();
                return t.name().equals(names[i]);
            }
        }
        return false;
    }
}
