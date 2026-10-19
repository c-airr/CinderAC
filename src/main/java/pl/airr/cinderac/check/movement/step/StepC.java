package pl.airr.cinderac.check.movement.step;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.MotionTable;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Step", type = 'C', complexType = "Motion", description = "Reverse step.")
public final class StepC extends Check {
    public StepC(PlayerData data) {
        super(data);
    }

    /** Minimum falling deltaY for air ticks 0..25 (0 and 1-2 have separate rules). */
    private static final float[] LIMIT = {0f, -0.0785f, -0.1553f, -0.2306f, -0.3044f, -0.3768f, -0.4475f, -0.5171f, -0.586f, -0.652f,
            -0.7171f, -0.7812f, -0.844f, -0.9055f, -0.9658f, -1.0249f, -1.0828f, -1.1395f, -1.1951f, -1.2496f, -1.303f, -1.356f, -1.407f,
            -1.457f, -1.507f, -1.555f};

    private int groundBelowTicks;

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        if (data.action.positionTicks < 30) return;
        PositionTracker pos = data.position;
        int air = pos.airTicks;
        double dy = pos.deltaY;
        double broken = data.action.distanceToLastBreak;
        if (broken < 1.25 && broken > 0.0) return;
        float fall = data.player == null ? 0.0f : data.player.getFallDistance();
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.DOOR, Exempt.DRAGON_DAMAGE, Exempt.DEATH, Exempt.JOINED, Exempt.FLIGHT,
                Exempt.CREATIVE, Exempt.GLIDING, Exempt.EXPLOSION, Exempt.CANCELLED_PLACE, Exempt.ELYTRA, Exempt.LIQUID, Exempt.VEHICLE,
                Exempt.RIPTIDE, Exempt.FISHING_ROD, Exempt.SLEEPING, Exempt.BLOCK_PLACE, Exempt.VELOCITY, Exempt.FAST, Exempt.SLIME,
                Exempt.ENDER_PEARL, Exempt.ANVIL, Exempt.END_ROD, Exempt.CHAIN, Exempt.DRIPSTONE, Exempt.SERVER_POSITION, Exempt.PISTON,
                Exempt.JUMP_BOOST, Exempt.CLIMBABLE, Exempt.SCAFFOLDING, Exempt.POWDER_SNOW, Exempt.SNOW)
                || pos.nearAmethyst || pos.nearDripstone || data.action.sinceEnderCrystalDamageTicks < 20;
        groundBelowTicks = MotionTable.groundBelow(pos) ? groundBelowTicks + 1 : 0;
        if (exempt || air < 0 || air >= LIMIT.length) return;
        String info = "ticks=" + air + " deltaY=" + dy + " distance=" + fall;
        if (air == 0) {
            if (dy < -0.5 && groundBelowTicks > 5) {
                if (increaseBuffer() > maxBuffer()) fail(info + " ng=" + groundBelowTicks);
            } else {
                decreaseBuffer();
            }
            return;
        }
        if (air <= 2) {
            if (dy < LIMIT[air]) {
                if (increaseBuffer() > maxBuffer()) fail(info);
            } else {
                decreaseBuffer();
            }
            return;
        }
        // an exceeded threshold falls through to the next one (as in the original) until one is not met
        for (int t = air; t < LIMIT.length; t++) {
            if (!(dy < LIMIT[t])) {
                decreaseBuffer();
                return;
            }
            if (increaseBuffer() > maxBuffer()) fail(info);
        }
    }
}
