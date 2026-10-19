package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Speed", type = 'C', complexType = "Air", description = "Moving too quickly in the air.")
public final class SpeedC extends Check {
    public SpeedC(PlayerData data) {
        super(data);
    }

    private static final Exempt[] INSTANT = {Exempt.SERVER_POSITION_FAST};

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        if (data.action.jumpBoostAmplifier > 50) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ, dy = pos.deltaY;
        int air = pos.airTicks;
        boolean ice = pos.sinceOnIceTicks < 40, above = pos.sinceBlockAboveTicks < 30;
        double max = 0.3625 * (data.action.movementSpeedAttribute / 0.1);
        if (above) max += 0.1;
        if (exempt(Exempt.CANCELLED_PLACE)) max += 0.1;
        if (pos.sinceSlimeAroundTicks < 15) max += 0.6;
        if (air == 2 && pos.sinceBlockAboveTicks < 50 && dy == -0.07840000152587834) max += 0.165;
        if (ice) max += 0.25;
        if (above && ice) max += 0.3;
        if (exempt(Exempt.STAIRS)) max += 0.05;
        if (pos.sinceSlimeTicks < 30) max += 0.1;
        if (ServerVersion.atLeast(16) && pos.sinceSoulSpeedTicks < 30) max += 0.3;
        int velocity = data.velocity.sinceVelocityTicks;
        max += data.velocity.maxVelocityXZ * Math.pow(0.915f, velocity);
        if (velocity < 10) max += 0.025;
        int explosion = data.velocity.sinceExplosionTicks;
        max += data.velocity.lastExplosionXZ * Math.pow(0.915f, explosion);
        if (explosion < 10) max += 0.025;
        if (ServerVersion.atLeast(13, 1) && pos.sinceHoneyTicks < 20) max += 0.125;
        if (data.action.sincePlaceIceTicks < 40) max += 0.2;
        if (exempt(Exempt.FARMLAND)) max += 0.025;
        if (ServerVersion.atLeast(9) && Settings.get().entityCollision && pos.sinceNearLivingEntityTicks < 20) max += 0.1;
        if (exempt(Exempt.PISTON)) max += 0.325;
        if (exempt(Exempt.SNOW)) max += 0.05;
        if (data.action.sinceFireballDamageTicks < 30) max += 0.75;
        int crystal = data.action.sinceEnderCrystalDamageTicks;
        if (crystal < 5) max += 1.0;
        else if (crystal > 5 && crystal < 25) max += 0.5;
        if (pos.sinceGlidingTicks < 30 || data.action.sinceGlidingTicks < 30) max += pos.lastTickDeltaXZ + 0.15;
        if (max < 0.36) max = 0.36;
        boolean exempt = exempt(Exempt.JOINED, Exempt.ILLEGAL_BLOCK, Exempt.RIPTIDE, Exempt.SHULKER, Exempt.CHORUS_FRUIT, Exempt.FLIGHT,
                Exempt.CREATIVE, Exempt.VEHICLE, Exempt.SHULKER_BOX, Exempt.CHUNK, Exempt.DRAGON_DAMAGE, Exempt.FISHING_ROD,
                Exempt.DOLPHINS_GRACE, Exempt.ENDER_PEARL, Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.FROZEN, Exempt.DEATH, Exempt.SLEEPING,
                Exempt.BOAT, Exempt.SPECTATOR, Exempt.GLIDING, Exempt.ANVIL, Exempt.FULLY_STUCK, Exempt.PARTIALLY_STUCK, Exempt.CANCELLED_MOVE,
                Exempt.ENTITY_CRAM_FIX);
        double diff = dxz - max;
        if (air > 1 && diff > cfg.getDouble("min-difference", 0.001) && !exempt) {
            if (increaseBuffer() > maxBuffer() || diff > 0.5 && diff < 100.0 && !exempt(INSTANT)) {
                fail("speed=" + dxz + " max=" + max + " diff=" + diff + " ticks=" + air + " deltaY=" + dy + " t=" + data.action.sinceGlidingTicks);
            }
        } else {
            decreaseBuffer();
        }
    }
}
