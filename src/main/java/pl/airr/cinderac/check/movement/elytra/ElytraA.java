package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'A', complexType = "Limit", description = "Gliding too quickly on X/Z axis.")
public final class ElytraA extends Check {
    public ElytraA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        double dxz = data.position.deltaXZ;
        boolean exempt = exempt(Exempt.RIPTIDE, Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.DEATH, Exempt.ENDER_PEARL, Exempt.CHORUS_FRUIT,
                Exempt.SERVER_POSITION_FAST, Exempt.DRAGON_DAMAGE, Exempt.DOLPHINS_GRACE) || data.position.sinceEntityCramTicks < 200;
        boolean riptide = data.position.sinceRiptideTicks < 400;
        if (dxz > cfg.getDouble("max-speed", 4.25) && !exempt && !riptide) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz);
        } else {
            decreaseBuffer();
        }
    }
}
