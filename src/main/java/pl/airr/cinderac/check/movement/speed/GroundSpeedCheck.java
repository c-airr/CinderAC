package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

/**
 * Shared horizontal speed limit based on ground ticks (SpeedB, SpeedD, ElytraN).
 * The three checks share the table and the core bonuses; they differ in the remaining bonuses.
 */
public abstract class GroundSpeedCheck extends Check {
    protected GroundSpeedCheck(PlayerData data) {
        super(data);
    }

    /** Table variant: SpeedD has different bonuses for blocks above and slabs. */
    protected boolean variantD() {
        return false;
    }

    /** true = skip the packet (unnatural jump boost). */
    protected boolean skip() {
        return data.action.jumpBoostAmplifier > 50;
    }

    /** Limit core shared by the three checks. */
    protected double baseLimit(int ticks, boolean stairs) {
        PositionTracker pos = data.position;
        boolean inside = pos.insideBlock || pos.insideBlock2;
        boolean above = pos.sinceBlockAboveTicks < 45, ice = pos.sinceOnIceTicks < 60, bed = pos.sinceBedTicks < 30;
        boolean slime = pos.sinceSlimeTicks < 30, trapdoor = pos.sinceTrapdoorTicks < 40, slab = pos.nearSlab;
        boolean fence = pos.sinceFenceTicks < 20, onIce = pos.sinceOnIceTicks == 0;
        boolean aboveFree = above && !inside, d = variantD();
        double max = 0.2975 * (data.action.movementSpeedAttribute / 0.1);
        switch (ticks) {
            case 0:
                max += 0.323;
                if (aboveFree) max += 0.076;
                if (ice) max += 0.08;
                if (stairs) max += 0.35;
                if (above && ice) max += 0.35;
                if (slime) max += 0.05;
                if (trapdoor && ice) max += 0.6;
                if (slab) max += 0.04;
                if (onIce && slab) max += 0.1;
                if (trapdoor) max += 0.1;
                if (fence) max += 0.1;
                if (pos.deltaY == 0.42f) max += 0.03;
                break;
            case 1:
                max += 0.025;
                if (aboveFree) max += 0.0728;
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
                if (aboveFree) max += 0.06694;
                if (ice) max += 0.08;
                if (bed) max += 0.04;
                if (stairs) max += 0.35;
                if (above && ice) max += 0.4;
                if (trapdoor && ice) max += 0.55;
                if (slab) max += d ? 0.05 : 0.15;
                if (trapdoor) max += 0.1;
                if (onIce && slab) max += 0.005;
                if (fence) max += 0.1;
                break;
            case 3:
                max += 0.0693;
                if (aboveFree) max += 0.036;
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
                if (aboveFree) max += d ? 0.0411 : 0.0211;
                if (ice) max += 0.03;
                if (stairs) max += 0.35;
                if (trapdoor) max += 0.1;
                if (trapdoor && ice) max += 0.35;
                if (above && ice) max += 0.35;
                if (slab) max += 0.04;
                break;
            case 5:
                max += 0.0205;
                if (aboveFree) max += d ? 0.0431 : 0.0231;
                if (ice) max += 0.03;
                if (stairs) max += 0.35;
                if (trapdoor && ice) max += 0.35;
                if (slab) max += 0.04;
                break;
            case 6:
                max += 0.009;
                if (aboveFree) max += d ? 0.0527 : 0.0027;
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
                if (onIce && slab) max += 0.02;
                break;
            case 9:
                max += 0.0025;
                if (ice) max += 0.02;
                break;
            default:
                break;
        }
        if (onIce && ticks > 2) max += 0.09;
        if (pos.sinceOnIceTicks < 30 && ticks >= 5 && ticks <= 25 && above) max += 0.15;
        if (exempt(Exempt.CANCELLED_PLACE)) max += 0.08;
        if (pos.sinceOnIceTicks < 30 && ticks > 8) max += 0.15;
        if (exempt(Exempt.DEPTH_STRIDER)) max += 0.1;
        boolean soulSpeed = ServerVersion.atLeast(16) && pos.sinceSoulSpeedTicks < 30;
        if (soulSpeed) max += 0.5;
        if (ice && slime) max += 0.35;
        if (slime && above) max += 0.1;
        if (ServerVersion.atLeast(9) && Settings.get().entityCollision && pos.sinceNearLivingEntityTicks < 20) max += 0.1;
        if (exempt(Exempt.PISTON)) max += 0.325;
        if (exempt(Exempt.FARMLAND)) max += 0.05;
        if (exempt(Exempt.SNOW)) max += d ? 0.1 : 0.05;
        if (d ? pos.sinceSlimeAroundTicks < 15 : pos.sinceSlimeAroundTicks < 25) max += d ? 0.6 : 1.5;
        if (data.action.sincePlaceIceTicks < (d ? 40 : 35)) max += 0.2;
        if (data.action.sinceRavagerDamageTicks < 30) max += 2.0;
        int crystal = data.action.sinceEnderCrystalDamageTicks;
        if (crystal < 5) max += 1.0;
        else if (crystal > 5 && crystal < 25) max += d ? 0.5 : 0.75;
        if (pos.nearPressurePlate) max += 0.08;
        if (pos.nearDoor && pos.sinceBlockAboveTicks < 10) max += d ? 0.15 : 0.1;
        return max;
    }

    /** Bonus shared by SpeedB and SpeedD (explosions, ice under a block, panes, slowness). */
    protected double modernExtras(int ticks) {
        PositionTracker pos = data.position;
        double max = data.velocity.lastExplosionXZ * Math.pow(0.915f, data.velocity.sinceExplosionTicks);
        if (pos.sinceBlockAboveTicks < 10 && pos.sinceOnIceTicks < 10) max += 0.55;
        if (pos.nearPane) max += 0.075;
        if (data.action.gravityAttribute > 0.2) max += 0.025;
        double attr = data.action.movementSpeedAttribute;
        if (attr < 0.1 && ticks <= 1) max += 0.1 - attr + 0.01;
        if (data.action.hasSlowness && ticks <= 1) max += data.action.slownessAmplifier * 0.05;
        max += data.velocity.maxVelocityXZ * Math.pow(0.915f, data.velocity.sinceVelocityTicks);
        // the original bonus for a recent Speed effect requires "since < 0", which never happens - omitted
        return max;
    }

    /** Shared ending: buffer, or an instant fail on a large difference. */
    protected void verdict(double diff, double minDiff, boolean exempt, Exempt[] instantExempt, String info) {
        if (diff > minDiff && !exempt) {
            if (increaseBuffer() > maxBuffer() || diff > 0.5 && diff < 100.0 && !exempt(instantExempt)) fail(info);
        } else {
            decreaseBuffer();
        }
    }
}
