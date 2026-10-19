package pl.airr.cinderac.check.movement.motion;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Motion", type = 'A', complexType = "Constant", description = "Repeated vertical motions.")
public final class MotionA extends Check {
    public MotionA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        double dy = data.position.deltaY;
        if (Math.abs(dy - 0.11760000228882461) <= 0.001) return;
        boolean climbable = data.position.sinceClimbableTicks < 20;
        boolean exempt = exempt(Exempt.HALF_BLOCK, Exempt.CLIMBABLE, Exempt.FLIGHT, Exempt.BED, Exempt.LIQUID, Exempt.TELEPORT, Exempt.VELOCITY,
                Exempt.ILLEGAL_BLOCK, Exempt.SLEEPING, Exempt.SOUL_SAND, Exempt.HONEY, Exempt.SLIME, Exempt.LEVITATION, Exempt.SPECTATOR,
                Exempt.SCAFFOLDING, Exempt.FENCE, Exempt.BUBBLE_COLUMN, Exempt.CREATIVE, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.ELYTRA,
                Exempt.GLIDING, Exempt.CANCELLED_PLACE, Exempt.LENIENT_SCAFFOLDING, Exempt.PLACED_CLIMBABLE, Exempt.KELP, Exempt.BOAT,
                Exempt.CHUNK, Exempt.TRAPDOOR, Exempt.POWDER_SNOW, Exempt.EMPTIED_BUCKET, Exempt.CAKE)
                || data.position.nearHeavyCore || data.position.nearFlowerPot;
        boolean repeated = dy > 0.0 && dy == data.position.lastDeltaY;
        if (repeated && !exempt && !climbable) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
