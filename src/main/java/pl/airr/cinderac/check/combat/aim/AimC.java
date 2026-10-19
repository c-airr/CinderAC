package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'C', complexType = "Repeated", description = "Repeated yaw values.")
public final class AimC extends Check {
    public AimC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || !recentlyAttacked(3)) return;
        float dy = data.rotation.deltaYaw, last = data.rotation.lastDeltaYaw;
        if (dy > 1.25f && last > 1.25f && dy == last) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
