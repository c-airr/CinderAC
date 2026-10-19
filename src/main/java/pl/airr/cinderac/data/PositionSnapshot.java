package pl.airr.cinderac.data;

/** Position from one movement packet (history of the last 5). */
public final class PositionSnapshot {
    public final double x, y, z;
    public double deltaX, deltaY, deltaZ, deltaXZ;
    public boolean onGround, teleport;

    public PositionSnapshot(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
