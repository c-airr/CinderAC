package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'P', complexType = "Yaw Acceleration", description = "Large yaw acceleration.")
public final class AimP extends Check {
    public AimP(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || !recentlyAttacked(3)) return;
        float pitch = data.rotation.pitch, yawAccel = data.rotation.yawAccel, pitchAccel = data.rotation.pitchAccel;
        double reach = data.combat.reachDistance;
        if (yawAccel > 20.0f && (double) pitchAccel < 0.05 && Math.abs(pitch) < 60.0f && reach > 1.0) {
            if (increaseBuffer() > maxBuffer()) fail("yawAccel=" + yawAccel + " pitchAccel=" + pitchAccel);
        } else {
            decreaseBuffer();
        }
    }
}
