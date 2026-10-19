package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'Q', complexType = "GCD Modulo", description = "GCD bypass flaw detected.")
public final class AimQ extends Check {
    public AimQ(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3 || !data.rotation.validSensitivity()) return;
        double sens = data.rotation.sensitivityValue;
        if (sens < 0.01) return;
        float f = (float) sens * 0.6f + 0.2f;
        float step = f * f * f * 1.2f;
        float yaw = data.rotation.yaw, pitch = data.rotation.pitch;
        float yawDiff = Math.abs(yaw - (yaw - yaw % step));
        float pitchDiff = Math.abs(pitch - (pitch - pitch % step));
        float combined = data.rotation.deltaYaw + data.rotation.deltaPitch;
        double dist = data.combat.targetDistance;
        boolean flaw = (yawDiff == 0.0f || pitchDiff == 0.0f) && combined > 0.75f;
        if (flaw && dist > 1.0 && !exempt(Exempt.CREATIVE, Exempt.SPECTATOR)) {
            if (increaseBuffer() > maxBuffer()) fail("yawDifference=" + yawDiff + " pitchDifference=" + pitchDiff + " combined=" + combined);
        } else {
            decreaseBuffer();
        }
    }
}
