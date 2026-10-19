package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Aim", type = 'E', complexType = "Ratio", description = "Invalid yaw change.")
public final class AimE extends Check {
    public AimE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        if (VMath.isScientific(dy) && dp > 0.125f && !exempt(Exempt.VEHICLE)) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
