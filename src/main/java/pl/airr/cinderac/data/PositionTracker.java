package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;
import pl.airr.cinderac.util.VMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Player position from movement packets + surroundings (3x4x3 blocks around the feet, nearby entities).
 * The block grid is computed by the PlayerMoveEvent listener on the player owner thread (Folia: region); read-only here.
 */
public final class PositionTracker {
    private final PlayerData data;

    public double x, y, z, lastX, lastY, lastZ, lastLastX, lastLastY, lastLastZ;
    public double deltaX, deltaY, deltaZ, deltaXZ, deltaXYZ;
    public double lastDeltaX, lastDeltaY, lastDeltaZ, lastDeltaXZ, lastLastDeltaY;
    public boolean onGround, lastOnGround, lastLastOnGround;
    public boolean positionChanged, lastPositionChanged, lastLastPositionChanged;
    public boolean mathematicallyOnGround, moved, atOrigin;
    public int blockX, blockY, blockZ;
    public World world;
    public Location location, lastLocation;
    public final LinkedList<PositionSnapshot> history = new LinkedList<>();

    // grid of 36 blocks (x -1..1, y -1..2, z -1..1) and single blocks below the feet - set from PlayerMoveEvent
    public volatile List<Material> nearMaterials;
    public volatile List<Material> ringBelow2;
    public volatile List<Material> vehicleMaterials;
    public volatile Material below1, below2, below3;
    public volatile List<Entity> nearEntities;

    public List<Material> blocksBelow, blocksAbove, blocksAround, blocksAtHead;
    public boolean onIce, insideBlock, insideBlock2, blockAbove, blocksAroundNotAir, solidNear, slabAbove;
    /** All blocks around and below are air. */
    public boolean allAir;
    public boolean solidBelow;
    public boolean liquidBelow;

    public boolean nearFence, nearLiquid, nearSlime, nearSoulSand, nearClimbable, nearWeb, nearLilyPad, nearDaylightDetector,
            nearBrewingStand, nearWall, nearStairs, nearSlab, nearHoney, nearScaffolding, nearTrapdoor, nearSkull,
            nearEndPortalFrame, nearCampfire, nearBerryBush, nearShulker, nearSnow, nearAnvil, nearEndRod, nearChain,
            nearPiston, nearDoor, nearCauldron, nearHopper, nearGate, nearFlowerPot, nearKelp, nearBell, nearBed, nearCarpet,
            nearLectern, nearTurtleEgg, nearSeaPickle, nearIce, nearConduit, nearRepeater, nearPane, nearFrostedIce,
            nearBubbleColumn, nearFarmland, nearSeagrass, nearChest, nearCake, nearAmethyst, nearDripstone, nearPowderSnow,
            nearPath, nearLantern, nearRail, nearCandle, nearSign, nearBamboo, nearPressurePlate, nearEnchantTable, nearSculk,
            nearSculkSensor, nearIronBars, nearDripleaf, nearCactus, nearHeavyCore, nearDriedGhast, nearBlueIce;
    /** Bubbles/liquid at feet or head level (grid indices 16/19). */
    public boolean bubbleAtBody;
    /** Ladder/vines at both feet and head level. */
    public boolean climbableAtBody;
    /** Cobweb at feet or head level. */
    public boolean webAtBody;
    /** Soul sand / honey in the feet block (grid index 16). */
    public boolean soulSandAtFeet, honeyAtFeet;
    public boolean liquidAtBody, fullySubmerged;
    public boolean nearBoat, nearShulkerEntity, nearLivingEntity, nearHappyGhast, nearWorldBorder;
    public boolean serverFlightAllowed;

    // vehicle (VehicleMove)
    public double vehicleX, vehicleY, vehicleZ, lastVehicleX, lastVehicleY, lastVehicleZ;
    public double vehicleDeltaX, vehicleDeltaY, vehicleDeltaZ, vehicleDeltaXZ, lastVehicleDeltaX, lastVehicleDeltaY, lastVehicleDeltaZ;
    public Entity vehicle, lastVehicle;
    public boolean vehicleIce, vehicleLiquid, vehiclePiston, vehicleSlime, vehicleBubble, vehicleBed, vehicleAllAir;
    public boolean vehicleNearLiving, vehicleNearHappyGhast;
    public int vehicleNearBoats;
    public int sinceVehicleIceTicks = 500, sinceVehiclePistonTicks = 100, sinceVehicleBedTicks = 1000, sinceVehicleLiquidTicks = 150,
            sinceVehicleSlimeTicks = 150, sinceVehicleBubbleTicks = 150, vehicleAirTicks;

    public int sinceVehicleTicks = 100, sinceFlyingTicks = 100, sinceTrapdoorTicks = 100, airTicks, groundTicks;
    public int sinceSwimmingTicks = 100, sinceRiptideTicks = 200, sinceGlidingTicks = 100, sinceOnIceTicks;
    public int sinceDolphinsGraceTicks = 100, sinceLevitationTicks = 100, sinceBubbleColumnTicks = 100, glidingTicks;
    public int sinceSlowFallingTicks = 100, allAirTicks, mathGroundTicks, sinceWebTicks = 100, sinceLiquidTicks = 100;
    public int climbableAtBodyTicks, sinceJumpBoostTicks = 100, sinceSlimeTicks = 100, sinceHoneyTicks = 100;
    public int sinceBlockAboveTicks = 100, sinceFenceTicks = 100, sinceSoulSpeedTicks = 100, sinceBedTicks = 100;
    public int vehicleTicks, sinceFireworkTicks = 200, sinceIceTicks = 100, sincePistonTicks = 100, sinceSpeedTicks = 100;
    public int sinceSlimeAroundTicks = 500, sincePistonAroundTicks = 500, blockX0;
    public int sinceFrostedIceTicks, sinceScaffoldingTicks = 100, sinceGhostBlockTicks = 500, lastAbilitiesTick;
    public int sinceShulkerTicks = 100, sinceWearingElytraTicks = 100, lastGhostBlockTick, sinceFarmlandTicks = 100;
    public int sinceStairsTicks, sinceSlabTicks, sinceNearLivingEntityTicks = 100, sinceHighSpeedAttributeTicks = 100;
    public int sinceHighFlySpeedTicks = 200, sinceSlabAroundTicks = 100, sinceWaterloggedTicks = 1000, lastWaterloggedTick;
    public int sinceClimbableTicks = 1000, sinceSetbackTicks = 1000, sinceBlocksAroundTicks = 100;
    public int sinceEntityCramTicks = 100, sinceSpectatorTicks = 100, liquidBelowTicks = 100, notSolidBelowTicks = 100;
    public int sinceBoatTicks = 100, sinceShulkerEntityTicks = 100, sinceTridentTicks = 1000, sinceHappyGhastTicks = 100;
    public int sinceFishedTicks = 1000, lastFishedTick, ghostBlockSetbacks, ticks1 = 1000, sinceMovementFlagTicks = 1000;
    public float walkSpeed = 0.2f, flySpeed = 0.2f;

    // last "legal" position (on ground) - setback target
    public double setbackX, setbackY, setbackZ;
    public double joinX, joinY, joinZ;
    private double ghostBuffer, unloadedBuffer;
    private final List<Runnable> afterPosition = new ArrayList<>();

    public PositionTracker(PlayerData data) {
        this.data = data;
    }

    public void handleFlying(WrapperPlayClientPlayerFlying f) {
        lastLastOnGround = lastOnGround;
        lastOnGround = onGround;
        onGround = f.isOnGround();
        lastLastPositionChanged = lastPositionChanged;
        lastPositionChanged = positionChanged;
        positionChanged = f.hasPositionChanged();
        if (data.action.flyingTicks % 100 == 0) ghostBlockSetbacks = 0;
        if (f.hasPositionChanged()) {
            if (history.size() == 5) history.pollLast();
            PositionSnapshot prev = snapshot(0);
            PositionSnapshot s = new PositionSnapshot(f.getLocation().getX(), f.getLocation().getY(), f.getLocation().getZ());
            s.onGround = f.isOnGround();
            s.deltaX = s.x - prev.x;
            s.deltaY = s.y - prev.y;
            s.deltaZ = s.z - prev.z;
            s.deltaXZ = VMath.hypot(s.deltaX, s.deltaZ);
            history.addFirst(s);

            atOrigin = blockX == 0 && blockY == 0 && blockZ == 0;
            Player p = data.player;
            world = p == null ? null : p.getWorld();
            lastLastX = lastX;
            lastLastY = lastY;
            lastLastZ = lastZ;
            lastX = x;
            lastY = y;
            lastZ = z;
            x = f.getLocation().getX();
            y = f.getLocation().getY();
            z = f.getLocation().getZ();
            lastLocation = location == null ? new Location(world, x, y, z) : location;
            location = new Location(world, x, y, z);
            lastLastDeltaY = lastDeltaY;
            lastDeltaX = deltaX;
            lastDeltaY = deltaY;
            lastDeltaZ = deltaZ;
            lastDeltaXZ = deltaXZ;
            deltaX = x - lastX;
            deltaY = y - lastY;
            deltaZ = z - lastZ;
            deltaXZ = VMath.hypot(deltaX, deltaZ);
            deltaXYZ = VMath.magnitude(deltaX, deltaY, deltaZ);
            moved = Math.abs(deltaX) > 0.0 || Math.abs(deltaZ) > 0.0 || Math.abs(deltaY) > 0.0;
            mathematicallyOnGround = y % 0.015625 == 0.0;
            if (p != null && p.getVehicle() == null) vehicleTicks = 0;
            updateCollisions();
            ghostBlockFix();
            unloadedChunkFix();
            updateTicks();
            if (data.action.movementSpeedAttribute > 0.11) sinceHighSpeedAttributeTicks = 0;
            if (Settings.get().velocityWorldBorder && world != null) updateWorldBorder();
        }
        updateGroundTicks();
    }

    /** i-th position snapshot from the newest (0); missing = last available or (0,0,0). */
    public PositionSnapshot snapshot(int index) {
        PositionSnapshot result = new PositionSnapshot(0, 0, 0);
        int i = 0;
        for (PositionSnapshot s : history) {
            result = s;
            if (i == index) break;
            i++;
        }
        return result;
    }

    private void updateWorldBorder() {
        try {
            WorldBorder border = world.getWorldBorder();
            double cx = border.getCenter().getX(), cz = border.getCenter().getZ(), half = border.getSize() / 2.0;
            double[] d = {Math.abs(x - cx - half), Math.abs(x - cx + half), Math.abs(z - cz - half), Math.abs(z - cz + half)};
            Arrays.sort(d);
            nearWorldBorder = d[0] < 1.0;
        } catch (RuntimeException ignored) {
            // Folia: the world border may be unavailable off-thread - keep the previous value
        }
    }

    private void updateGroundTicks() {
        ++ticks1;
        ++sinceGhostBlockTicks;
        ++sinceFishedTicks;
        ++sinceHighSpeedAttributeTicks;
        ++sinceSetbackTicks;
        groundTicks = onGround ? groundTicks + 1 : 0;
        airTicks = !onGround ? airTicks + 1 : 0;
    }

    private void updateCollisions() {
        List<Material> near = nearMaterials;
        if (near == null || below1 == null || below2 == null || below3 == null) return;
        blockX = floor(x);
        blockY = floor(y);
        blockZ = floor(z);
        if (near.isEmpty()) {
            blocksBelow = blocksAbove = blocksAround = blocksAtHead = Collections.emptyList();
        } else {
            blocksBelow = pick(near, 0, 1, 2, 12, 13, 14, 24, 25, 26);
            blocksAbove = pick(near, 9, 10, 11, 21, 22, 23, 33, 34, 35);
            blocksAround = pick(near, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 19, 20, 27, 28, 29, 30, 31, 32);
            blocksAtHead = pick(near, 6, 7, 8, 18, 19, 20, 30, 31, 32);
        }
        boolean full = !near.isEmpty();
        onIce = full && (Blocks.ICE.is(near.get(13)) || Blocks.ICE.is(below2) || Blocks.ICE.is(below3));
        insideBlock = full && Blocks.isSolid(near.get(16)) && Blocks.isSolid(near.get(19));
        insideBlock2 = insideBlock;
        blockAbove = !Blocks.all(blocksAbove, Blocks.AIR);
        blocksAroundNotAir = !Blocks.all(blocksAround, Blocks.AIR);
        solidNear = Blocks.anySolid(near);
        allAir = Blocks.all(blocksAround, Blocks.AIR) && Blocks.all(blocksBelow, Blocks.AIR);
        updateNearBlocks(near);
    }

    private static List<Material> pick(List<Material> near, int... idx) {
        List<Material> out = new ArrayList<>(idx.length);
        for (int i : idx) out.add(near.get(i));
        return out;
    }

    private void updateNearBlocks(List<Material> near) {
        slabAbove = Blocks.any(blocksAbove, Blocks.SLAB);
        onIce = Blocks.any(blocksBelow, Blocks.ICE);
        List<Entity> entities = nearEntities;
        if (entities == null) entities = Collections.emptyList();
        nearBlueIce = nearDriedGhast = nearHeavyCore = nearCactus = nearPowderSnow = nearDripstone = nearCake = nearChest = false;
        nearSeagrass = nearFarmland = nearBubbleColumn = nearFrostedIce = nearSign = nearPressurePlate = nearEnchantTable = false;
        nearAmethyst = nearSculkSensor = nearSculk = nearHoney = nearScaffolding = nearChain = nearEndRod = nearTurtleEgg = false;
        nearKelp = nearShulker = nearSeaPickle = nearBell = nearLectern = nearBamboo = nearIronBars = nearLantern = false;
        nearConduit = nearBerryBush = nearCampfire = nearBed = nearSkull = nearPane = nearPiston = nearLilyPad = nearDoor = false;
        nearHopper = nearSlab = nearDripleaf = nearRail = nearRepeater = nearAnvil = nearSnow = nearGate = nearIce = false;
        nearStairs = nearCauldron = nearWall = nearBrewingStand = nearDaylightDetector = nearCandle = nearPath = false;
        nearEndPortalFrame = nearTrapdoor = nearFlowerPot = nearWeb = nearClimbable = nearSoulSand = nearCarpet = false;
        nearLiquid = nearFence = nearSlime = false;
        for (Material m : near) {
            nearSlime |= Blocks.SLIME_BLOCK.is(m);
            nearLiquid |= Blocks.LIQUID_BLOCK.is(m);
            nearWeb |= Blocks.WEB.is(m);
            nearFlowerPot |= Blocks.FLOWER_POT.is(m);
            nearEndPortalFrame |= Blocks.END_PORTAL_FRAME.is(m);
            nearDaylightDetector |= Blocks.DAYLIGHT_DETECTOR.is(m);
            nearBrewingStand |= Blocks.BREWING_STAND.is(m);
            nearIce |= Blocks.ICE.is(m);
            nearSnow |= Blocks.SNOW.is(m);
            nearAnvil |= Blocks.ANVIL.is(m);
            nearRepeater |= Blocks.REPEATER.is(m);
            nearHopper |= Blocks.HOPPER.is(m);
            nearLilyPad |= Blocks.LILY_PAD.is(m);
            nearPiston |= Blocks.PISTON.is(m);
            nearConduit |= Blocks.CONDUIT.is(m);
            nearSeaPickle |= Blocks.SEA_PICKLE.is(m);
            nearKelp |= Blocks.KELP.is(m);
            nearTurtleEgg |= Blocks.TURTLE_EGG.is(m);
            nearEndRod |= Blocks.END_ROD.is(m);
            nearScaffolding |= Blocks.SCAFFOLDING.is(m);
            nearHoney |= Blocks.HONEY_BLOCK.is(m);
            nearCauldron |= Blocks.CAULDRON.is(m);
            nearCake |= Blocks.CAKE.is(m);
            nearIronBars |= Blocks.IRON_BARS.is(m);
            nearEnchantTable |= Blocks.ENCHANT.is(m);
            nearAmethyst |= Blocks.AMETHYST.is(m);
            nearRail |= Blocks.RAIL.is(m);
            nearLantern |= Blocks.LANTERN.is(m);
            nearPowderSnow |= Blocks.POWDER_SNOW.is(m);
            nearDripstone |= Blocks.DRIPSTONE.is(m);
            nearChest |= Blocks.CHEST.is(m);
            nearSeagrass |= Blocks.SEAGRASS.is(m);
            nearBell |= Blocks.BELL.is(m);
            nearChain |= Blocks.CHAIN.is(m);
            nearLectern |= Blocks.LECTERN.is(m);
            nearBerryBush |= Blocks.BERRY_BUSH.is(m);
            nearBubbleColumn |= Blocks.BUBBLE_COLUMN.is(m);
            nearFrostedIce |= Blocks.FROSTED_ICE.is(m);
            nearSoulSand |= Blocks.SOUL_SAND_SOIL.is(m);
            nearCarpet |= Blocks.CARPET.is(m);
            nearTrapdoor |= Blocks.TRAPDOOR.is(m);
            nearWall |= Blocks.WALL.is(m);
            nearFarmland |= Blocks.FARMLAND.is(m);
            nearStairs |= Blocks.STAIRS.is(m);
            nearPressurePlate |= Blocks.PLATE.is(m);
            nearFence |= Blocks.FENCE.is(m);
            nearClimbable |= Blocks.CLIMBABLE.is(m);
            nearGate |= Blocks.GATE.is(m);
            nearSlab |= Blocks.SLAB.is(m);
            nearDoor |= Blocks.DOOR.is(m);
            nearPane |= Blocks.PANE.is(m);
            nearSkull |= Blocks.SKULL.is(m);
            nearBed |= Blocks.BED.is(m);
            nearCampfire |= Blocks.CAMPFIRE.is(m);
            nearShulker |= Blocks.SHULKER.is(m);
            nearPath |= Blocks.PATH.is(m);
            nearSign |= Blocks.SIGN.is(m);
            nearBamboo |= Blocks.BAMBOO.is(m);
            nearSculk |= Blocks.SCULK.is(m);
            nearSculkSensor |= Blocks.SCULK_SENSOR.is(m);
            nearCandle |= Blocks.CANDLE.is(m);
            nearDripleaf |= Blocks.DRIPLEAF.is(m);
            nearCactus |= Blocks.CACTUS.is(m);
            nearHeavyCore |= Blocks.HEAVY_CORE.is(m);
            nearDriedGhast |= Blocks.DRIED_GHAST.is(m);
            nearBlueIce |= Blocks.BLUE_ICE.is(m);
        }
        nearHappyGhast = nearLivingEntity = nearShulkerEntity = nearBoat = false;
        for (Entity e : entities) {
            nearBoat |= e instanceof Boat || e.getType().name().contains("BOAT") || e.getType().name().contains("RAFT");
            nearShulkerEntity |= e.getType().name().equals("SHULKER");
            nearHappyGhast |= e.getType().name().equals("HAPPY_GHAST");
            if (e instanceof LivingEntity) nearLivingEntity = true;
            if (e instanceof Firework) sinceFireworkTicks = 0;
            if (e instanceof EnderDragon || e instanceof EnderDragonPart) data.action.sinceDragonDamageTicks = 0;
        }
        if (entities.size() > Settings.get().entityCramAmount) sinceEntityCramTicks = 0;
        boolean full = !near.isEmpty();
        bubbleAtBody = full && (Blocks.BUBBLE_COLUMN.is(near.get(16)) || Blocks.BUBBLE_COLUMN.is(near.get(19)));
        climbableAtBody = full && Blocks.CLIMBABLE.is(near.get(16)) && Blocks.CLIMBABLE.is(near.get(19));
        webAtBody = full && (Blocks.WEB.is(near.get(16)) || Blocks.WEB.is(near.get(19)));
        soulSandAtFeet = full && Blocks.SOUL_SAND.is(near.get(16));
        honeyAtFeet = full && Blocks.HONEY_BLOCK.is(near.get(16));
        liquidAtBody = full && (Blocks.LIQUID.is(near.get(16)) || Blocks.LIQUID.is(near.get(19)));
        fullySubmerged = full && Blocks.LIQUID.is(near.get(16)) && Blocks.LIQUID.is(near.get(19));
        if (nearBoat || nearShulkerEntity || nearTrapdoor || nearCarpet || nearCampfire || nearBrewingStand || nearRepeater
                || nearDaylightDetector || nearSkull || nearLilyPad || nearShulker || nearFlowerPot || nearHappyGhast) {
            allAirTicks = 0;
        }
    }

    private void updateTicks() {
        ++sinceMovementFlagTicks;
        ++sinceEntityCramTicks;
        allAirTicks = allAir ? allAirTicks + 1 : 0;
        mathGroundTicks = mathematicallyOnGround ? mathGroundTicks + 1 : 0;
        solidBelow = below1 != null && below1.isSolid() && !Blocks.LIQUID.is(below1);
        notSolidBelowTicks = !solidBelow ? notSolidBelowTicks + 1 : 0;
        sinceHighFlySpeedTicks = flySpeed > 0.11f ? 0 : sinceHighFlySpeedTicks + 1;
        if (nearSlime || Blocks.SLIME_BLOCK.is(below1) || Blocks.SLIME_BLOCK.is(below2) || Blocks.SLIME_BLOCK.is(below3)) {
            sinceSlimeTicks = 0;
        } else {
            ++sinceSlimeTicks;
        }
        ++sinceWaterloggedTicks;
        ActionTracker act = data.action;
        Player p = data.player;
        sinceIceTicks = nearIce ? 0 : sinceIceTicks + 1;
        sinceBoatTicks = nearBoat ? 0 : sinceBoatTicks + 1;
        sinceShulkerEntityTicks = nearShulkerEntity ? 0 : sinceShulkerEntityTicks + 1;
        sinceSpeedTicks = act.hasSpeed ? 0 : sinceSpeedTicks + 1;
        sinceSpectatorTicks = p != null && p.getGameMode() == GameMode.SPECTATOR ? 0 : sinceSpectatorTicks + 1;
        sinceBedTicks = nearBed ? 0 : sinceBedTicks + 1;
        sinceFarmlandTicks = nearFarmland ? 0 : sinceFarmlandTicks + 1;
        sinceSlabTicks = nearSlab ? 0 : sinceSlabTicks + 1;
        sinceHappyGhastTicks = nearHappyGhast ? 0 : sinceHappyGhastTicks + 1;
        sincePistonTicks = nearPiston ? 0 : sincePistonTicks + 1;
        sinceNearLivingEntityTicks = nearLivingEntity ? 0 : sinceNearLivingEntityTicks + 1;
        sinceJumpBoostTicks = act.hasJumpBoost ? 0 : sinceJumpBoostTicks + 1;
        sinceBlockAboveTicks = blockAbove ? 0 : sinceBlockAboveTicks + 1;
        sinceBlocksAroundTicks = blocksAroundNotAir ? 0 : sinceBlocksAroundTicks + 1;
        sinceFrostedIceTicks = nearFrostedIce ? 0 : sinceFrostedIceTicks + 1;
        sinceFenceTicks = nearFence ? 0 : sinceFenceTicks + 1;
        sinceOnIceTicks = onIce ? 0 : sinceOnIceTicks + 1;
        sinceTrapdoorTicks = nearTrapdoor ? 0 : sinceTrapdoorTicks + 1;
        sinceWebTicks = nearWeb ? 0 : sinceWebTicks + 1;
        sinceClimbableTicks = nearClimbable ? 0 : sinceClimbableTicks + 1;
        sinceShulkerTicks = nearShulkerEntity || nearShulker ? 0 : sinceShulkerTicks + 1;
        sinceScaffoldingTicks = nearScaffolding ? 0 : sinceScaffoldingTicks + 1;
        sinceLiquidTicks = liquidAtBody || nearLiquid || bubbleAtBody || nearBubbleColumn ? 0 : sinceLiquidTicks + 1;
        climbableAtBodyTicks = climbableAtBody ? climbableAtBodyTicks + 1 : 0;
        sinceStairsTicks = nearStairs ? 0 : sinceStairsTicks + 1;
        sinceSlimeAroundTicks = Blocks.any(blocksAround, Blocks.SLIME_BLOCK) ? 0 : sinceSlimeAroundTicks + 1;
        sincePistonAroundTicks = Blocks.any(blocksAround, Blocks.PISTON) ? 0 : sincePistonAroundTicks + 1;
        sinceSlabAroundTicks = Blocks.any(blocksAround, Blocks.SLAB) ? 0 : sinceSlabAroundTicks + 1;
        liquidBelow = blocksBelow != null && Blocks.all(blocksBelow, Blocks.LIQUID);
        liquidBelowTicks = liquidBelow ? liquidBelowTicks + 1 : 0;
        if (p != null) sinceVehicleTicks = p.isInsideVehicle() || p.getVehicle() != null ? 0 : sinceVehicleTicks + 1;
        ++sinceFireworkTicks;
        if (p != null) sinceFlyingTicks = p.getAllowFlight() || serverFlightAllowed ? 0 : sinceFlyingTicks + 1;
        if (ServerVersion.atLeast(9)) {
            sinceSlowFallingTicks = act.hasSlowFalling ? 0 : sinceSlowFallingTicks + 1;
            sinceSwimmingTicks = bukkitSwimming(p) || act.swimming ? 0 : sinceSwimmingTicks + 1;
            sinceGlidingTicks = act.glidingBukkit || act.gliding ? 0 : sinceGlidingTicks + 1;
            boolean glidingNow = p != null && p.isGliding();
            glidingTicks = glidingNow ? glidingTicks + 1 : 0;
            if (glidingNow || act.gliding) lastTickDeltaXZ = deltaXZ;
            sinceSoulSpeedTicks = ServerVersion.atLeast(16) && nearSoulSand && hasSoulSpeed(act.boots) ? 0 : sinceSoulSpeedTicks + 1;
            sinceWearingElytraTicks = act.wearingElytra ? 0 : sinceWearingElytraTicks + 1;
            sinceRiptideTicks = bukkitRiptiding(p) || act.riptiding ? 0 : sinceRiptideTicks + 1;
            sinceHoneyTicks = nearHoney ? 0 : sinceHoneyTicks + 1;
            sinceLevitationTicks = act.hasLevitation ? 0 : sinceLevitationTicks + 1;
            sinceDolphinsGraceTicks = bukkitDolphinsGrace(p) ? 0 : sinceDolphinsGraceTicks + 1;
            sinceBubbleColumnTicks = bubbleAtBody || nearBubbleColumn ? 0 : sinceBubbleColumnTicks + 1;
            sinceTridentTicks = holdingTrident(p) ? 0 : sinceTridentTicks + 1;
        }
    }

    public double lastTickDeltaXZ;

    private static boolean hasSoulSpeed(ItemStack boots) {
        if (boots == null) return false;
        Enchantment soul = Enchantment.getByName("SOUL_SPEED");
        return soul != null && boots.getEnchantmentLevel(soul) > 0;
    }

    static boolean bukkitSwimming(Player p) {
        return p != null && ServerVersion.atLeast(13, 1) && p.isSwimming();
    }

    static boolean bukkitDolphinsGrace(Player p) {
        if (p == null || !ServerVersion.atLeast(13, 1)) return false;
        org.bukkit.potion.PotionEffectType t = org.bukkit.potion.PotionEffectType.getByName("DOLPHINS_GRACE");
        return t != null && p.hasPotionEffect(t);
    }

    static boolean bukkitRiptiding(Player p) {
        return p != null && ServerVersion.atLeast(13, 1) && p.isRiptiding();
    }

    private static boolean holdingTrident(Player p) {
        if (p == null) return false;
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();
        return main.getType().name().equals("TRIDENT") || off.getType().name().equals("TRIDENT");
    }

    private void ghostBlockFix() {
        Player p = data.player;
        Settings s = Settings.get();
        if (p == null) return;
        if (s.ghostBlocksEnabled && CinderAC.get().tps() < s.ghostBlocksMinTps) return;
        if (!s.ghostBlocksEnabled) return;
        if (data.clientVersion().isOlderThan(ClientVersion.V_1_17) && ServerVersion.atLeast(17) && s.ghostBlocksIgnoreLegacyVia && y < 5.0) {
            return;
        }
        boolean exempt = data.exempt(Exempt.LAGGED_NEAR_GROUND, Exempt.BLOCK_PLACE_FAST, Exempt.NOT_MOVING,
                Exempt.LAGGED_NEAR_GROUND_MODERN, Exempt.FLIGHT, Exempt.TELEPORT, Exempt.SWIMMING, Exempt.DEATH);
        boolean bedrock = data.isBedrock();
        boolean ghost = allAirTicks > s.ghostBlocksTicks && groundTicks >= 1 && mathGroundTicks >= 1;
        if (ghost && !exempt && !bedrock) {
            if (++ghostBuffer > s.ghostBlocksBufferMax) {
                if (s.ghostBlocksSetback && (setbackX != 0.0 || setbackY != 0.0 || setbackZ != 0.0)) {
                    data.pendingSetback = true;
                    sinceGhostBlockTicks = 0;
                    ++ghostBlockSetbacks;
                    lastGhostBlockTick = CinderAC.get().tick();
                }
                if (s.ghostBlocksMessageEnabled) data.sendMessage(s.ghostBlocksMessage);
                String msg = s.ghostBlocksStaffMessage.replace("%x%", Integer.toString(blockX)).replace("%y%", Integer.toString(blockY))
                        .replace("%z%", Integer.toString(blockZ)).replace("%ticks%", Integer.toString(allAirTicks))
                        .replace("%world%", p.getWorld().getName()).replace("%player%", data.name);
                if (s.ghostBlocksStaffMessageEnabled) CinderAC.get().alerts().staff(msg);
                if (s.ghostBlocksConsole) CinderAC.get().getLogger().info(pl.airr.cinderac.util.Text.strip(msg));
                allAirTicks = 0;
                ghostBuffer = 0.0;
            }
        } else if (ghostBuffer > 0.0) {
            ghostBuffer -= s.ghostBlocksBufferDecay;
        }
    }

    private void unloadedChunkFix() {
        Settings s = Settings.get();
        if (!s.unloadedChunksSetback || data.player == null) return;
        boolean exempt = data.exempt(Exempt.JOINED, Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.LIQUID, Exempt.CHUNK,
                Exempt.FLIGHT, Exempt.ELYTRA, Exempt.GLIDING, Exempt.SLOW_FALLING)
                || data.action.positionTicks < 20 || data.action.acceptedServerPosition;
        double diff = Math.abs(Math.abs(deltaY) - 0.09800000190734881);
        if (deltaY < (double) -0.09f && !onGround && !exempt && diff < 0.005) {
            if (++unloadedBuffer > s.unloadedChunksMaxTicks) {
                data.teleport(new Location(world, setbackX, setbackY, setbackZ, data.rotation.yaw, data.rotation.pitch));
                if (!s.unloadedChunksMessage.isEmpty()) {
                    CinderAC.get().alerts().staff(s.unloadedChunksMessage.replace("%player%", data.name)
                            .replace("%x%", Integer.toString(blockX)).replace("%y%", Integer.toString(blockY))
                            .replace("%z%", Integer.toString(blockZ)).replace("%ticks%", Integer.toString(allAirTicks))
                            .replace("%world%", data.player.getWorld().getName()));
                }
                unloadedBuffer = 0.0;
            } else if (unloadedBuffer > 0.0) {
                unloadedBuffer -= 0.025;
            }
        }
    }

    public void handleVehicleMove(WrapperPlayClientVehicleMove v) {
        ++vehicleTicks;
        lastVehicle = vehicle;
        Player p = data.player;
        if (p != null) vehicle = p.getVehicle();
        if (lastVehicle != vehicle) vehicleTicks = 0;
        vehicleX = v.getPosition().getX();
        vehicleY = v.getPosition().getY();
        vehicleZ = v.getPosition().getZ();
        lastVehicleDeltaX = vehicleDeltaX;
        lastVehicleDeltaY = vehicleDeltaY;
        lastVehicleDeltaZ = vehicleDeltaZ;
        vehicleDeltaX = vehicleX - lastVehicleX;
        vehicleDeltaY = vehicleY - lastVehicleY;
        vehicleDeltaZ = vehicleZ - lastVehicleZ;
        vehicleDeltaXZ = VMath.hypot(vehicleDeltaX, vehicleDeltaZ);
        List<Material> vm = vehicleMaterials;
        if (vm != null) {
            vehicleBed = vehicleBubble = vehicleSlime = vehiclePiston = vehicleLiquid = vehicleIce = false;
            for (Material m : vm) {
                vehicleIce |= Blocks.ICE.is(m);
                vehicleLiquid |= Blocks.LIQUID.is(m);
                vehiclePiston |= Blocks.PISTON.is(m);
                vehicleSlime |= Blocks.SLIME_BLOCK.is(m);
                vehicleBubble |= Blocks.BUBBLE_COLUMN.is(m);
                vehicleBed |= Blocks.BED.is(m);
            }
            vehicleAllAir = Blocks.all(vm, Blocks.AIR);
        }
        vehicleNearBoats = 0;
        vehicleNearLiving = false;
        vehicleNearHappyGhast = false;
        List<Entity> entities = nearEntities;
        if (entities != null) {
            for (Entity e : entities) {
                if (e instanceof Boat) ++vehicleNearBoats;
                if (vehicle != null && isPassenger(vehicle, e)) continue;
                if (vehicle != null && vehicle.getEntityId() == e.getEntityId()) continue;
                if (e instanceof LivingEntity) vehicleNearLiving = true;
                if (e.getType().name().equals("HAPPY_GHAST")) vehicleNearHappyGhast = true;
            }
        }
        sinceVehicleIceTicks = vehicleIce ? 0 : sinceVehicleIceTicks + 1;
        sinceVehiclePistonTicks = vehiclePiston ? 0 : sinceVehiclePistonTicks + 1;
        sinceVehicleBedTicks = vehicleBed ? 0 : sinceVehicleBedTicks + 1;
        sinceVehicleLiquidTicks = vehicleLiquid ? 0 : sinceVehicleLiquidTicks + 1;
        sinceVehicleSlimeTicks = vehicleSlime ? 0 : sinceVehicleSlimeTicks + 1;
        sinceVehicleBubbleTicks = vehicleBubble ? 0 : sinceVehicleBubbleTicks + 1;
        vehicleAirTicks = vehicleAllAir ? vehicleAirTicks + 1 : 0;
        if (vehicleNearHappyGhast) vehicleAirTicks = 0;
        lastVehicleX = vehicleX;
        lastVehicleY = vehicleY;
        lastVehicleZ = vehicleZ;
    }

    @SuppressWarnings("deprecation")
    private static boolean isPassenger(Entity vehicle, Entity e) {
        return ServerVersion.atLeast(11) ? vehicle.getPassengers().contains(e) : e.equals(vehicle.getPassenger());
    }

    public void handleDigging(WrapperPlayClientPlayerDigging d) {
        Player p = data.player;
        if (p == null) return;
        if (ServerVersion.atLeast(9) && d.getAction() == DiggingAction.RELEASE_USE_ITEM) {
            boolean wet = sinceLiquidTicks < 30 || sinceWaterloggedTicks < 30;
            boolean rain = p.getPlayerWeather() == WeatherType.DOWNFALL || p.getWorld().hasStorm();
            boolean bucket = data.action.sinceBucketTicks < 20;
            if ((wet || rain || bucket || data.action.raining) && sinceTridentTicks < 20) sinceRiptideTicks = 0;
        }
    }

    /** Whether there is something below (block at y-1 or y-2 is not air). */
    public boolean nearGround() {
        return below1 != null && below2 != null && (!Blocks.isAir(below1) || !Blocks.isAir(below2));
    }

    public boolean nearGroundModern() {
        return below1 != null && below2 != null && below3 != null
                && (!Blocks.isAir(below1) || !Blocks.isAir(below2) || !Blocks.isAir(below3));
    }

    /** Only air below (blocksBelow and the y-2 ring). */
    public boolean airBelow() {
        List<Material> ring = ringBelow2;
        if (blocksBelow == null || ring == null) return false;
        return Blocks.all(blocksBelow, Blocks.AIR) && Blocks.all(ring, Blocks.AIR);
    }

    public double accelXZ() {
        return Math.abs(deltaXZ - lastDeltaXZ);
    }

    /** A fishing hook caught the player. */
    public void onFished() {
        lastFishedTick = CinderAC.get().tick();
        sinceFishedTicks = 0;
    }

    public void onWaterlogged() {
        sinceWaterloggedTicks = 0;
        lastWaterloggedTick = CinderAC.get().tick();
    }

    public void onFireworkHeld(Player p) {
        if (!ServerVersion.atLeast(9) || p == null) return;
        if (Blocks.FIREWORK.is(p.getInventory().getItemInMainHand().getType())
                || Blocks.FIREWORK.is(p.getInventory().getItemInOffHand().getType())) {
            sinceFireworkTicks = 0;
        }
    }

    public void handleAbilities(WrapperPlayServerPlayerAbilities a) {
        sinceFlyingTicks = 0;
        lastAbilitiesTick = CinderAC.get().tick();
        serverFlightAllowed = a.isFlightAllowed();
        flySpeed = a.getFlySpeed() * 2.0f;
    }

    /** Requests a setback to the last legal position (if known). */
    public void requestSetback() {
        if (y < setbackY && Settings.get().dontSetbackLowerPositions) return;
        if (setbackX == 0.0 && setbackY == 0.0 && setbackZ == 0.0) return;
        sinceSetbackTicks = 0;
        data.pendingSetback = true;
    }

    public void runAfterPosition(Runnable r) {
        afterPosition.add(r);
    }

    void flushAfterPosition() {
        for (Runnable r : afterPosition) r.run();
        afterPosition.clear();
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }
}
