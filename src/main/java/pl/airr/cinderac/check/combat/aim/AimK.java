package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Aim", type = 'K', complexType = "Linear", description = "Not constant rotations.")
public final class AimK extends Check {
    public AimK(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        float lastDy = data.rotation.lastDeltaYaw, lastDp = data.rotation.lastDeltaPitch;
        double gcdYaw = VMath.gcd((long) ((double) dy * VMath.EXPANDER), (long) ((double) lastDy * VMath.EXPANDER)) / VMath.EXPANDER;
        double gcdPitch = VMath.gcd((long) ((double) dp * VMath.EXPANDER), (long) ((double) lastDp * VMath.EXPANDER)) / VMath.EXPANDER;
        double yawSteps = (double) dy / gcdYaw, pitchSteps = (double) dp / gcdPitch;
        double lastYawSteps = (double) lastDy / gcdYaw, lastPitchSteps = (double) lastDp / gcdPitch;
        if (!(dy > 0.1f) || !(dp > 0.1f) || !(dy < 20.0f) || !(dp < 20.0f)) return;
        double yawMod = yawSteps % lastYawSteps, pitchMod = pitchSteps % lastPitchSteps;
        double yawFrac = Math.abs(Math.floor(yawMod) - yawMod), pitchFrac = Math.abs(Math.floor(pitchMod) - pitchMod);
        boolean yaw = yawMod > 60.0 && yawFrac > 0.1;
        boolean pitch = pitchMod > 60.0 && pitchFrac > 0.1;
        double sens = data.rotation.sensitivity;
        boolean normalSens = sens < 100.0 && sens > -1.0;
        if (yaw && pitch && !exempt(Exempt.CINEMATIC) && !normalSens) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
