package pl.airr.cinderac.check.movement.flight;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'E', complexType = "Hover", description = "Invalid Y-Axis movement.")
public final class FlightE extends Check {
    public FlightE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (cfg.getBoolean("ignore-ghost-blocks", true) && pos.onGround && pos.allAir && pos.mathematicallyOnGround) return;
        if (data.action.gravityAttribute != 0.08) return;
        double dy = pos.deltaY, accel = Math.abs(dy - pos.lastDeltaY);
        int ticks = pos.allAirTicks;
        boolean invalid = ticks > 3 && Math.abs(dy) < 0.5 && accel < 0.05 && pos.allAir;
        boolean exempt = exempt(Exempt.BED, Exempt.EMPTIED_BUCKET, Exempt.WEB, Exempt.LAGGED_NEAR_GROUND, Exempt.SLEEPING, Exempt.JOINED_CHUNK_LOAD,
                Exempt.ILLEGAL_BLOCK, Exempt.BOAT, Exempt.SHULKER, Exempt.BLOCK_PLACE, Exempt.LAGGED_NEAR_GROUND_MODERN, Exempt.TRAPDOOR,
                Exempt.WORLD_CHANGE, Exempt.NOT_MOVING, Exempt.LILY_PAD, Exempt.SPECTATOR, Exempt.SHULKER_BOX, Exempt.GLIDING, Exempt.ELYTRA,
                Exempt.HONEY, Exempt.CANCELLED_PLACE, Exempt.FENCE, Exempt.SWEET_BERRIES, Exempt.DEAD,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.JOINED, Exempt.LIQUID, Exempt.LEVITATION, Exempt.SLOW_FALLING, Exempt.RIPTIDE,
                Exempt.VEHICLE, Exempt.LENIENT_SCAFFOLDING, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHUNK, Exempt.COMBO_MODE,
                Exempt.MYTHIC_MOB, Exempt.CLIMBABLE) || pos.slabAbove
                || data.velocity.sinceExplosionTicks < 20 || data.velocity.sinceVelocityTicks < 20 || data.velocity.sinceVelocityAppliedTicks < 20;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("ticks=" + ticks + " acceleration=" + accel + " deltaY=" + dy + " delay=" + data.keepAlive.flyingDelayMillis + " tp=" + data.action.sinceServerPositionTicks + " vel=" + data.velocity.sinceVelocityTicks);
        } else {
            decreaseBuffer();
        }
    }
}
