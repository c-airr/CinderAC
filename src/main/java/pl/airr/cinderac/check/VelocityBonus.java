package pl.airr.cinderac.check;

/** Speed limit bonus after knockback, decreasing with ticks since the velocity. */
public final class VelocityBonus {
    private VelocityBonus() {
    }

    public static double of(int sinceVelocityTicks, double maxVelocityXZ) {
        int t = sinceVelocityTicks;
        if (t >= 0 && t <= 7) return maxVelocityXZ + 0.05;
        if (t >= 8 && t <= 15) return maxVelocityXZ * 0.75;
        if (t >= 16 && t <= 20) return maxVelocityXZ * 0.6;
        if (t >= 21 && t <= 25) return maxVelocityXZ * 0.5;
        if (t >= 26 && t <= 30) return maxVelocityXZ * 0.35;
        if (t >= 31 && t <= 35) return maxVelocityXZ * 0.25;
        if (t >= 36 && t <= 40) return maxVelocityXZ * 0.1;
        return 0.0;
    }
}
