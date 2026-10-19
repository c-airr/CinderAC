package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'B', complexType = "Acceleration", description = "Invalid Y-Axis change.")
public final class ElytraB extends Check {
    public ElytraB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        int ticks = data.position.glidingTicks;
        double dxz = data.position.deltaXZ, accelXZ = Math.abs(dxz - data.position.lastDeltaXZ);
        double dy = data.position.deltaY, accelY = Math.abs(dy - data.position.lastDeltaY);
        boolean exempt = exempt(Exempt.FIREWORK, Exempt.LIQUID);
        boolean invalid = ticks > 5 && ticks < 400 && accelXZ < 1.0E-10 && accelY < 1.0E-10 && dxz > 1.0;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("ticks=" + ticks + " deltaXZ=" + dxz + " deltaY=" + dy + " accelXZ=" + accelXZ + " accelY=" + accelY);
        } else {
            decreaseBuffer();
        }
    }
}
