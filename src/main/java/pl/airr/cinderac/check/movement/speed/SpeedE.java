package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Speed", type = 'E', complexType = "Prediction", description = "Invalid friction.")
public final class SpeedE extends Check {
    public SpeedE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ;
        double min = 1.0E-4, predicted = pos.lastDeltaXZ * 0.91f;
        if (pos.sinceLiquidTicks < 20) {
            min += 0.1;
            if (pos.sinceDolphinsGraceTicks < 20) min += 0.75;
            if (pos.sinceDolphinsGraceTicks < 20 && exempt(Exempt.DEPTH_STRIDER)) min += 1.0;
            if (data.velocity.sinceExplosionTicks < 20) min += data.velocity.explosionXZSum + 0.05;
        }
        if (pos.sinceSlimeAroundTicks < 10) predicted += 0.35f;
        if (pos.sinceNearLivingEntityTicks < 20) min += 0.155;
        if (pos.lastLastOnGround) predicted *= 0.6f;
        if (data.velocity.hasPendingVelocity) predicted += data.velocity.maxVelocityXZ + 0.025;
        if (data.velocity.hasPendingExplosion) predicted += data.velocity.explosionXZSum + 0.025;
        if (pos.sincePistonTicks < 20) predicted += 0.3;
        if (!pos.lastOnGround) {
            predicted += 0.025999999f;
        } else {
            if (!pos.onGround && pos.deltaY >= 0.0 || data.action.jumpBoostAmplifier < 0) predicted += 0.2f;
            predicted += data.action.movementSpeedAttribute * 1.3f * 0.16277136f / (float) Math.pow(0.54600006f, 3.0);
        }
        if (data.action.sinceGlidingTicks < 20) predicted += pos.lastTickDeltaXZ;
        if (pos.sinceIceTicks < 20 && pos.sinceTrapdoorTicks < 20) min += 0.41;
        if (pos.sinceIceTicks < 15) min += 0.31;
        if (pos.sinceSlimeTicks < 15) min += 0.21;
        if (pos.sinceFarmlandTicks < 15) min += 0.31;
        if (data.action.swiftSneak && data.action.sincePlaceBlockTicks < 20) min += 0.1;
        if (exempt(Exempt.FARMLAND)) min += 0.05;
        if (pos.sinceIceTicks < 15 && pos.sinceBlockAboveTicks < 15) min += 0.35;
        if (pos.sinceSwimmingTicks < 20 && data.action.sincePlaceBlockTicks < 20) min += 0.15;
        if (data.velocity.sinceHighVelocityTicks < 25 && pos.sinceOnIceTicks < 25) min += 0.525;
        if (data.action.sinceSoulPlaceTicks < 20 && data.action.swiftSneak && pos.sinceSoulSpeedTicks < 20) min += 0.05;
        double diff = dxz - predicted;
        double floor = Math.max(0.2625, data.action.movementSpeedAttribute * (pos.onGround && pos.lastOnGround ? 2.0 : 1.125));
        boolean invalid = diff > min && dxz > floor;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.TELEPORT, Exempt.SLEEPING, Exempt.GLIDING, Exempt.RIPTIDE, Exempt.WEB, Exempt.CREATIVE,
                Exempt.ENDER_PEARL, Exempt.VEHICLE, Exempt.WORLD_CHANGE, Exempt.CHUNK, Exempt.HONEY, Exempt.CHORUS_FRUIT)
                || pos.sincePistonTicks < 20;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("dXZ=" + dxz + " diff=" + diff + " min=" + min + " tp=" + data.action.sinceServerPositionTicks + " g=" + data.action.gliding + " v=" + data.velocity.sinceVelocityTicks);
        } else {
            decreaseBuffer();
        }
    }
}
