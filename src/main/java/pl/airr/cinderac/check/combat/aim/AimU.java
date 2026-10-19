package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'U', complexType = "GCD Flaw", description = "GCD bypass.")
public final class AimU extends Check {
    public AimU(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3 || !data.rotation.validSensitivity()) return;
        float f = (float) data.rotation.sensitivityValue * 0.6f + 0.2f;
        float step = f * f * f * 1.2f;
        float yaw = data.rotation.yaw, pitch = data.rotation.pitch;
        float yawDiff = Math.abs(yaw - (yaw - yaw % step)), pitchDiff = Math.abs(pitch - (pitch - pitch % step));
        if (yawDiff == 0.01f || pitchDiff == 0.01f) fail("[I] yawDiff=" + yawDiff + " pitchDiff=" + pitchDiff);
    }
}
