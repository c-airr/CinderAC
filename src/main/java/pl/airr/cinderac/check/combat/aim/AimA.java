package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Aim", type = 'A', complexType = "Slope", description = "Invalid pitch change.")
public final class AimA extends Check {
    public AimA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        if (VMath.isScientific(dp) && dy > 0.5f) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
