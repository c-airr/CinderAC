package pl.airr.cinderac.check.movement.flight;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'A', complexType = "Prediction [S]", description = "Invalid Y-Axis movement.")
public final class FlightA extends Check {
    public FlightA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (cfg.getBoolean("ignore-ghost-blocks", true) && pos.onGround && pos.allAir && pos.mathematicallyOnGround) return;
        if (data.velocity.hasPendingVelocity) return;
        int ticks = pos.allAirTicks;
        double dy = pos.deltaY;
        if (Math.abs(dy - 0.11760000228882461) <= 0.001 && exempt(Exempt.FAST)) return;
        if (dy + 0.09800000190734881 <= 0.001) return;
        double diff = Math.abs(dy - (pos.lastDeltaY - data.action.gravityAttribute) * 0.98f);
        boolean velocity = data.velocity.sinceVelocityTicks < 20;
        if (ticks < 30 && exempt(Exempt.PISTON)) return;
        int limit = 6;
        if (data.action.hasJumpBoost) limit += data.action.jumpBoostAmplifier;
        boolean invalid = ticks > limit && diff > 1.0E-8 && ticks > 6;
        boolean exempt = exempt(Exempt.SHULKER_BOX, Exempt.WEB, Exempt.BOAT, Exempt.ILLEGAL_BLOCK, Exempt.SLEEPING, Exempt.SLOW_FALLING,
                Exempt.SHULKER, Exempt.TRAPDOOR, Exempt.FISHING_ROD, Exempt.LILY_PAD, Exempt.SPECTATOR, Exempt.HONEY, Exempt.CHORUS_FRUIT,
                Exempt.BED, Exempt.CHAIN, Exempt.WORLD_CHANGE, Exempt.CANCELLED_PLACE, Exempt.GLIDING, Exempt.ELYTRA,
                Exempt.LAGGED_NEAR_GROUND_MODERN, Exempt.EMPTIED_BUCKET, Exempt.DEAD, Exempt.CANCELLED_BREAK, Exempt.FENCE,
                Exempt.LAGGED_NEAR_GROUND, Exempt.BLOCK_PLACE_FAST, Exempt.GLASS_PANE, Exempt.NOT_MOVING,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.JOINED, Exempt.LIQUID, Exempt.LEVITATION, Exempt.SLOW_FALLING, Exempt.RIPTIDE,
                Exempt.VEHICLE, Exempt.LENIENT_SCAFFOLDING, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHUNK, Exempt.COMBO_MODE,
                Exempt.MYTHIC_MOB, Exempt.CLIMBABLE) || pos.slabAbove;
        if (invalid && !exempt && !velocity) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " difference=" + diff + " ticks=" + ticks + " delay=" + data.keepAlive.flyingDelayMillis + " tp=" + data.action.sinceServerPositionTicks + " vel=" + data.velocity.sinceVelocityTicks);
        } else {
            decreaseBuffer();
        }
    }
}
