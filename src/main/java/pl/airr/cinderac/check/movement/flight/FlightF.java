package pl.airr.cinderac.check.movement.flight;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionSnapshot;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'F', complexType = "Prediction", description = "Invalid Y-Axis movement.")
public final class FlightF extends Check {
    public FlightF(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (data.action.jumpBoostAmplifier > 50 || data.action.gliding) return;
        PositionSnapshot now = pos.snapshot(0), prev = pos.snapshot(1);
        int air = pos.airTicks;
        double dy = now.deltaY, lastDy = prev.deltaY;
        double predicted = predict(prev);
        double diff = Math.abs(dy - predicted);
        double tolerance = 1.0E-8;
        if (data.action.sinceGravityChangeTicks < 20) return;
        if (data.action.sinceServerPositionTicks == 1) tolerance += 0.01;
        if (air == 7) tolerance += 0.03;
        if (Math.abs(dy - 0.40444491418477924) <= 0.025 && data.action.sincePlaceBlockTicks < 20) tolerance += 0.75;
        if (pos.sinceGlidingTicks < 10) tolerance += 0.6;
        if (data.action.sinceGlidingTicks < 30) tolerance += 0.1;
        if (data.clientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            // 1.8 does not send positions without movement, so the prediction from history is less reliable
            if (!pos.lastPositionChanged || !pos.lastLastPositionChanged) tolerance += 0.03;
            if (data.action.sincePlaceBlockTicks < 20 && data.rotation.pitch < 45.0f) tolerance += 0.02;
            if (air == 6 && pos.deltaXZ < 0.1) tolerance += 0.1;
            if (air == 7 && pos.deltaXZ < 0.1) tolerance += 0.2;
            if (air == 7 && Math.abs(dy - (0.0 - data.action.gravityAttribute) * 0.98f) < 5.0E-4) tolerance += 0.2;
        }
        boolean invalid = !now.teleport && !now.onGround && !prev.onGround && diff > tolerance
                && !data.velocity.hasPendingVelocity && !data.velocity.hasPendingExplosion;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.LIQUID, Exempt.WALL, Exempt.CONDUIT, Exempt.FARMLAND, Exempt.SLOW_FALLING, Exempt.LEVITATION,
                Exempt.RIPTIDE, Exempt.WEB, Exempt.WATERLOGGED, Exempt.SHULKER_BOX, Exempt.SPECTATOR, Exempt.SLEEPING, Exempt.FISHING_ROD,
                Exempt.SLIME, Exempt.AROUND_SLIME, Exempt.CHUNK, Exempt.PLACED_SLIME, Exempt.POWDER_SNOW, Exempt.SEAGRASS, Exempt.SCAFFOLDING,
                Exempt.CLIMBABLE, Exempt.SWIMMING, Exempt.SHULKER, Exempt.SIGN, Exempt.JOINED_CHUNK_LOAD, Exempt.EMPTIED_BUCKET,
                Exempt.SWEET_BERRIES, Exempt.BOAT, Exempt.HONEY, Exempt.SEA_PICKLE, Exempt.ENDER_PEARL, Exempt.TELEPORT, Exempt.BUBBLE_COLUMN,
                Exempt.VEHICLE, Exempt.CHEST, Exempt.PLACED_WEB, Exempt.KELP, Exempt.GLIDING, Exempt.GLITCHED_BLOCKS_ABOVE)
                || pos.nearDriedGhast || pos.sincePistonTicks < 20 || pos.sinceBlockAboveTicks < 20 || pos.slabAbove;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer() || diff > 1.0 && pos.sinceBlocksAroundTicks > 20) {
                fail("dY=" + dy + " ldY=" + lastDy + " pre=" + predicted + " diff=" + diff + " tol=" + tolerance + " tick=" + air
                        + " tp=" + data.action.sinceServerPositionTicks + " t=" + data.action.sinceGlidingTicks + " v=" + data.velocity.sinceVelocityTicks);
            }
        } else {
            decreaseBuffer();
        }
    }

    /** Expected deltaY: previous minus gravity, 0.98 drag, tiny values zeroed as in the client. */
    private double predict(PositionSnapshot prev) {
        if (prev.teleport) return 0.0;
        double v = (prev.deltaY - data.action.gravityAttribute) * 0.98f;
        boolean legacy = data.clientVersion().isOlderThan(ClientVersion.V_1_9);
        return Math.abs(v) < (legacy ? 0.005 : 0.003) ? 0.0 : v;
    }
}
