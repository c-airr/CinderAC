package pl.airr.cinderac.check.movement.motion;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Motion", type = 'C', complexType = "Jump", description = "Invalid jump motion.")
public final class MotionC extends Check {
    public MotionC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        double dy = Math.abs(data.position.deltaY);
        double diff = Math.abs(dy - 0.42f);
        boolean exempt = exempt(Exempt.JUMP_BOOST, Exempt.VELOCITY, Exempt.FLIGHT, Exempt.ILLEGAL_BLOCK, Exempt.CREATIVE, Exempt.SLIME, Exempt.GLIDING,
                Exempt.WEB, Exempt.VEHICLE, Exempt.TELEPORT, Exempt.SCAFFOLDING, Exempt.LIQUID, Exempt.SLAB, Exempt.STAIRS, Exempt.ELYTRA);
        // almost but not exactly the jump velocity - typical for a modified jump
        boolean invalid = diff < 1.0E-5 && diff > 0.0 && dy > 0.1;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
