package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.check.VelocityBonus;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Invalid", type = 'J', complexType = "Motion", description = "Moving too quickly.", experimental = true)
public final class InvalidJ extends Check {
    public InvalidJ(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        boolean inside = data.position.insideBlock || data.position.insideBlock2;
        if (!inside) return;
        if (data.action.jumpBoostAmplifier > 50) return;
        if (data.position.nearDripstone || data.position.nearChain || data.position.nearLantern) return;
        double dxz = data.position.deltaXZ, dy = data.position.deltaY;
        int ground = data.position.groundTicks;
        int sinceIce = data.position.sinceOnIceTicks;
        boolean above = data.position.sinceBlockAboveTicks < 45;
        boolean ice = sinceIce < 60;
        boolean bed = data.position.sinceBedTicks < 30;
        boolean stairs = data.position.nearStairs;
        boolean slime = data.position.sinceSlimeTicks < 30;
        boolean trapdoor = data.position.sinceTrapdoorTicks < 40;
        boolean depth = exempt(Exempt.DEPTH_STRIDER);
        boolean slab = data.position.nearSlab;
        boolean fence = data.position.sinceFenceTicks < 20;
        boolean soul = data.position.sinceSoulSpeedTicks < 30;
        double max = 0.2975 * (data.action.movementSpeedAttribute / 0.1);
        switch (ground) {
            case 0:
                max += 0.03;
                if (ice) max += 0.08;
                if (stairs) max += 0.35;
                if (above && ice) max += 0.35;
                if (slime) max += 0.05;
                if (trapdoor && ice) max += 0.6;
                if (slab) max += 0.04;
                if (sinceIce == 0 && slab) max += 0.1;
                if (trapdoor) max += 0.1;
                if (fence) max += 0.1;
                if (dy == 0.41999998688697815) max += 0.03;
                break;
            case 1:
                max += 0.025;
                if (ice) max += 0.1;
                if (stairs) max += 0.35;
                if (above && ice) max += 0.45;
                if (slime) max += 0.05;
                if (trapdoor && ice) max += 0.55;
                if (slab) max += 0.15;
                if (trapdoor) max += 0.1;
                if (fence) max += 0.1;
                break;
            case 2:
                max += 0.134;
                if (above && !inside) max += 0.06694;
                if (ice) max += 0.08;
                if (bed) max += 0.04;
                if (stairs) max += 0.35;
                if (above && ice) max += 0.4;
                if (trapdoor && ice) max += 0.55;
                if (slab) max += 0.15;
                if (trapdoor) max += 0.1;
                if (sinceIce == 0 && slab) max += 0.005;
                if (fence) max += 0.1;
                break;
            case 3:
                max += 0.0693;
                if (above && !inside) max += 0.036;
                if (ice) max += 0.045;
                if (stairs) max += 0.35;
                if (trapdoor && ice) max += 0.35;
                if (above && ice) max += 0.35;
                if (slab) max += 0.04;
                if (bed) max += 0.04;
                if (fence) max += 0.1;
                if (trapdoor) max += 0.1;
                break;
            case 4:
                max += 0.035;
                if (above && !inside) max += 0.0211;
                if (ice) max += 0.03;
                if (stairs) max += 0.35;
                if (trapdoor) max += 0.1;
                if (trapdoor && ice) max += 0.35;
                if (above && ice) max += 0.35;
                if (slab) max += 0.04;
                break;
            case 5:
                max += 0.0205;
                if (above && !inside) max += 0.0231;
                if (ice) max += 0.03;
                if (stairs) max += 0.35;
                if (trapdoor && ice) max += 0.35;
                if (slab) max += 0.04;
                break;
            case 6:
                max += 0.009;
                if (above && !inside) max += 0.0027;
                if (ice) max += 0.02;
                if (trapdoor && ice) max += 0.35;
                if (slab) max += 0.04;
                break;
            case 7:
                max += 0.005;
                if (ice) max += 0.02;
                if (trapdoor && ice) max += 0.1;
                break;
            case 8:
                max += 0.003;
                if (ice) max += 0.02;
                if (sinceIce == 0 && slab) max += 0.02;
                break;
            case 9:
                max += 0.0025;
                if (ice) max += 0.02;
                break;
            default:
                break;
        }
        if (sinceIce == 0 && ground > 2) max += 0.09;
        if (exempt(Exempt.FARMLAND)) max += 0.05;
        if (sinceIce < 30 && ground >= 5 && ground <= 25 && above) max += 0.15;
        if (exempt(Exempt.CANCELLED_PLACE)) max += 0.08;
        if (sinceIce < 30 && ground > 8) max += 0.15;
        if (depth) max += 0.1;
        if (ServerVersion.atLeast(16) && soul && data.action.speedAmplifier > 3) max += 1.25;
        if (ServerVersion.atLeast(16) && soul) max += 0.5;
        if (ice && slime) max += 0.35;
        if (slime && above) max += 0.1;
        if (exempt(Exempt.SLAB)) max += 0.3;
        if (exempt(Exempt.STAIRS)) max += 0.5;
        max += VelocityBonus.of(data.velocity.sinceVelocityTicks, data.velocity.maxVelocityXZ);
        if (data.position.sinceSlimeAroundTicks < 15) max += 0.7;
        if (ServerVersion.atLeast(9) && Settings.get().entityCollision && data.position.sinceNearLivingEntityTicks < 20) max += 0.1;
        if (data.action.sincePlaceIceTicks < 35) max += 0.2;
        if (exempt(Exempt.PISTON)) max += 0.325;
        if (data.position.sinceSlabAroundTicks < 5) max += 0.025;
        if (exempt(Exempt.SNOW)) max += 0.05;
        if (data.action.sinceRavagerDamageTicks < 30) max += 2.0;
        int crystal = data.action.sinceEnderCrystalDamageTicks;
        if (crystal < 5) max += 0.65;
        else if (crystal > 5 && crystal < 25) max += 0.3;
        if (data.position.sinceSpeedTicks < 10) max += (double) data.action.lastSpeedAmplifier * 0.075;
        double diff = dxz - max;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.JOINED, Exempt.CREATIVE, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.BAMBOO, Exempt.CAKE,
                Exempt.PRESSURE_PLATE, Exempt.CHUNK, Exempt.DOLPHINS_GRACE, Exempt.TELEPORT, Exempt.ILLEGAL_BLOCK, Exempt.GLIDING, Exempt.DOOR,
                Exempt.RIPTIDE, Exempt.VEHICLE, Exempt.BOAT, Exempt.FISHING_ROD, Exempt.ELYTRA, Exempt.SIGN, Exempt.DEATH, Exempt.SLEEPING,
                Exempt.ENDER_PEARL, Exempt.FROZEN, Exempt.CHORUS_FRUIT, Exempt.SPECTATOR, Exempt.WORLD_CHANGE, Exempt.ANVIL,
                Exempt.CANCELLED_MOVE, Exempt.GLASS_PANE, Exempt.FENCE, Exempt.BED)
                || data.position.nearSculk || data.position.nearIronBars || data.position.nearDaylightDetector;
        // threshold from checks.movement.speed.b.min-difference (shared with SpeedB)
        double minDiff = CinderAC.get().checkConfig("checks.movement.speed.b").getDouble("min-difference", 0.001);
        if (diff > minDiff && ground >= 0 && !exempt) {
            if (increaseBuffer() > maxBuffer() || diff > 0.5 && diff < 100.0 && !exempt(Exempt.SERVER_POSITION, Exempt.CHORUS_FRUIT)) {
                fail("speed=" + dxz + " max=" + max + " diff=" + diff + " ticks=" + ground + " deltaY=" + dy);
            }
        } else {
            decreaseBuffer();
        }
    }
}
