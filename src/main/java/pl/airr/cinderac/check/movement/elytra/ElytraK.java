package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'K', complexType = "Acceleration", description = "Accelerating while ascending.")
public final class ElytraK extends Check {
    public ElytraK(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        double dy = data.position.deltaY, dxz = data.position.deltaXZ;
        boolean accel = dxz - data.position.lastDeltaXZ > 0.005;
        int ticks = data.position.glidingTicks;
        boolean exempt = exempt(Exempt.FIREWORK, Exempt.EXPLOSION, Exempt.RIPTIDE, Exempt.DRAGON_DAMAGE, Exempt.FLIGHT, Exempt.VELOCITY,
                Exempt.SLIME, Exempt.DOLPHINS_GRACE, Exempt.LIQUID);
        if (dxz > 0.3 && dy > 0.0 && accel && !exempt && ticks > 5) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz + " accel=" + accel + " ticks=" + ticks);
        } else {
            decreaseBuffer();
        }
    }
}
