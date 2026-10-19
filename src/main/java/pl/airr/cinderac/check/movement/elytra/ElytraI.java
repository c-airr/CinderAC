package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'I', complexType = "Motion", description = "Invalid motion.")
public final class ElytraI extends Check {
    public ElytraI(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        double dxz = data.position.deltaXZ;
        double rest = 0.1 % (dxz % 0.1);
        boolean exempt = exempt(Exempt.FIREWORK, Exempt.EXPLOSION, Exempt.RIPTIDE, Exempt.WEB, Exempt.DRAGON_DAMAGE, Exempt.FLIGHT,
                Exempt.VELOCITY, Exempt.LIQUID);
        if (rest < 1.0E-8 && !exempt && dxz > 0.1) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz);
        } else {
            decreaseBuffer();
        }
    }
}
