package pl.airr.cinderac.check.movement.motion;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Motion", type = 'B', complexType = "Inverse", description = "Repeated vertical motions.")
public final class MotionB extends Check {
    public MotionB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        double dy = data.position.deltaY;
        boolean consumed = data.action.sinceConsumeTicks < 50;
        boolean near = data.position.nearLectern || data.position.nearRepeater;
        boolean exempt = exempt(Exempt.WEB, Exempt.LILY_PAD, Exempt.JOINED, Exempt.ELYTRA, Exempt.SIGN, Exempt.ANVIL, Exempt.SHULKER, Exempt.SHULKER_BOX,
                Exempt.PLACED_WEB, Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.CREATIVE, Exempt.SPECTATOR, Exempt.BOAT, Exempt.WORLD_CHANGE,
                Exempt.GLIDING, Exempt.ENDER_PEARL, Exempt.LIQUID, Exempt.CHORUS_FRUIT, Exempt.TRAPDOOR, Exempt.CHUNK, Exempt.PISTON,
                Exempt.ILLEGAL_BLOCK, Exempt.VELOCITY, Exempt.LENIENT_SCAFFOLDING, Exempt.FARMLAND, Exempt.COLLIDING_VERTICALLY, Exempt.STAIRS,
                Exempt.SLAB, Exempt.SLOW_FALLING, Exempt.BED);
        boolean inverse = dy > 0.0 && dy == -data.position.lastDeltaY;
        if (inverse && !exempt && !consumed && !near) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
