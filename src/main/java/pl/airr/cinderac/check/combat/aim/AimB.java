package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'B', complexType = "Modulo", description = "Invalid yaw change.")
public final class AimB extends Check {
    public AimB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dy = data.rotation.deltaYaw;
        if (dy > 0.0f && ((double) dy % 0.25 == 0.0 || (double) dy % 0.1 == 0.0)) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
