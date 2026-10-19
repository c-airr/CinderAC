package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'G', complexType = "Acceleration", description = "Invalid acceleration.")
public final class ElytraG extends Check {
    public ElytraG(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        double dy = data.position.deltaY, dxz = data.position.deltaXZ;
        double accel = dxz - data.position.lastDeltaXZ;
        boolean invalid = accel > 0.031 && dy > -0.5 && dxz > 0.65;
        boolean exempt = exempt(Exempt.FIREWORK, Exempt.EXPLOSION, Exempt.RIPTIDE, Exempt.CHUNK, Exempt.DRAGON_DAMAGE, Exempt.FLIGHT,
                Exempt.VELOCITY, Exempt.SLIME, Exempt.DOLPHINS_GRACE);
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("accel=" + accel + " deltaXZ=" + dxz + " deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
