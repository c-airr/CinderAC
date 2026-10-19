package pl.airr.cinderac.check;

import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.util.Blocks;

/** Expected vanilla vertical motion: after a jump (air tick 1..11) and when falling off an edge (1..7). */
public final class MotionTable {
    private static final double[] JUMP = {0, 0.41999998688697815, 0.33319999363422426, 0.24813599859094637, 0.164773281826065,
            0.08307781780646906, -0.07840000152587834, -0.15523200451659847, -0.23052736891296632, -0.30431682745754074,
            -0.37663049823865435, -0.1040803780930446};
    private static final double[] FALL = {0, -0.07840000152587834, -0.15523200451659847, -0.23052736891296632,
            -0.30431682745754074, -0.37663049823865435, -0.44749789698342113, -0.5169479491049742};

    /** Vertical motion on "ghost water" (the water is gone on the server, the client still swims). */
    private static final double[] GHOST_WATER = {0.05999999821185753, 0.051999998867515274, 0.06159999881982969, 0.06927999889612124,
            0.07542399904870933, 0.08033919924402255, 0.08427135945886732, 0.0874170876776148, 0.08993367029011523, 0.0519469373041872,
            -0.05647059355944606, 0.03812980539822064, -0.035014067535591664, -0.04453032983624894, 0.019999999105927202,
            -0.07159953051526458, 0.020820931761605266, 0.0010261658043049238, -0.023717291273619878, -0.010724939925282229};

    private MotionTable() {
    }

    /** null when there is no entry for this tick (same as a missing map key in the original). */
    public static Double jump(int airTicks) {
        return airTicks >= 1 && airTicks < JUMP.length ? JUMP[airTicks] : null;
    }

    public static Double fall(int airTicks) {
        return airTicks >= 1 && airTicks < FALL.length ? FALL[airTicks] : null;
    }

    /** Something other than air is 1 or 2 blocks below the feet. */
    public static boolean groundBelow(PositionTracker pos) {
        return pos.below1 != null && pos.below2 != null && (!Blocks.isAir(pos.below1) || !Blocks.isAir(pos.below2));
    }

    /** true = the motion matches ghost water and the check should skip it (ghost-blocks-fix.ghost-water-fix). */
    public static boolean ghostWater(PositionTracker pos, double dy) {
        if (!Settings.get().ghostWaterFix || pos.deltaXZ >= 0.15 || !groundBelow(pos)) return false;
        for (double v : GHOST_WATER) {
            if (Math.abs(dy) > 0.0 && Math.abs(dy - v) < 0.01) return true;
        }
        return false;
    }
}
