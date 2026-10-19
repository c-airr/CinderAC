package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Aim", type = 'I', complexType = "Constant", description = "Not constant rotations.")
public final class AimI extends Check {
    public AimI(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dp = data.rotation.deltaPitch, last = data.rotation.lastDeltaPitch, dy = data.rotation.deltaYaw;
        long gcd = VMath.gcd((long) (VMath.EXPANDER * (double) dp), (long) (VMath.EXPANDER * (double) last));
        boolean cinematic = exempt(Exempt.CINEMATIC);
        boolean low = data.rotation.lowSensitivity();
        boolean range = dy > 0.25f && dp > 0.25f && dp < 20.0f && dy < 20.0f;
        if (!cinematic && gcd < 131072L && range && !low) {
            if (increaseBuffer() > maxBuffer()) fail("rotation=" + gcd / 1000L + " deltaPitch=" + dp + " deltaYaw=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
