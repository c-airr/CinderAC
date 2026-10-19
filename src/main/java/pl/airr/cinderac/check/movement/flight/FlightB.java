package pl.airr.cinderac.check.movement.flight;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.MotionTable;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'B', complexType = "Prediction [C]", description = "Invalid Y-Axis movement.")
public final class FlightB extends Check {
    public FlightB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (data.action.jumpBoostAmplifier > 50) return;
        int air = pos.airTicks;
        double dy = pos.deltaY;
        if (Math.abs(dy - 0.11760000228882461) <= 0.001) return;
        double diff = Math.abs(dy - (pos.lastDeltaY - data.action.gravityAttribute) * 0.98f);
        if (dy + 0.09800000190734881 <= 0.005) return;
        if (data.action.sinceGravityChangeTicks < 20) return;
        boolean velocity = data.velocity.sinceVelocityTicks < 20;
        boolean exempt = exempt(Exempt.BOAT, Exempt.PARTIALLY_STUCK, Exempt.DEAD, Exempt.CHAIN, Exempt.WEB, Exempt.PLACING, Exempt.WATERLOGGED,
                Exempt.SLIME, Exempt.WORLD_CHANGE, Exempt.HONEY, Exempt.FISHING_ROD, Exempt.EMPTIED_BUCKET, Exempt.SWEET_BERRIES,
                Exempt.BUBBLE_COLUMN, Exempt.SCAFFOLDING, Exempt.NOT_MOVING, Exempt.SEAGRASS, Exempt.SIGN, Exempt.ANVIL, Exempt.END_ROD,
                Exempt.SPECTATOR, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.FENCE, Exempt.KELP, Exempt.BLOCK_BREAK, Exempt.FENCE_GATE,
                Exempt.JOINED_CHUNK_LOAD, Exempt.DOLPHINS_GRACE, Exempt.PLACED_WEB, Exempt.WALL, Exempt.SEA_PICKLE, Exempt.CHEST,
                Exempt.DRIPSTONE, Exempt.POWDER_SNOW, Exempt.FULLY_STUCK, Exempt.GLIDING, Exempt.ELYTRA, Exempt.BED, Exempt.SLEEPING,
                Exempt.CANCELLED_MOVE, Exempt.CONDUIT,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.JOINED, Exempt.LIQUID, Exempt.LEVITATION, Exempt.SLOW_FALLING, Exempt.RIPTIDE,
                Exempt.VEHICLE, Exempt.LENIENT_SCAFFOLDING, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHUNK, Exempt.COMBO_MODE,
                Exempt.MYTHIC_MOB, Exempt.CLIMBABLE) || pos.nearCandle || pos.slabAbove || pos.nearDriedGhast;
        if (MotionTable.ghostWater(pos, dy)) return;
        int limit = 7;
        if (data.action.hasJumpBoost) limit += data.action.jumpBoostAmplifier;
        boolean invalid = air > limit && diff > 1.0E-8;
        if (invalid && !exempt && !velocity && !data.velocity.hasPendingVelocity) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " difference=" + diff + " ticks=" + air + " delay=" + data.keepAlive.flyingDelayMillis + " tp=" + data.action.sinceServerPositionTicks + " vel=" + data.velocity.sinceVelocityTicks);
        } else {
            decreaseBuffer();
        }
    }
}
