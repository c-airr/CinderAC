package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'L', complexType = "Direction", description = "Switching directions too quickly.")
public final class AimL extends Check {
    public AimL(PlayerData data) {
        super(data);
    }

    private float lastDiff;
    private int sameDirection;

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || !recentlyAttacked(3)) return;
        float diff = data.rotation.pitch - data.rotation.lastPitch;
        if (diff < 0.0f && lastDiff > 0.0f || diff > 0.0f && lastDiff < 0.0f) sameDirection = 0;
        else ++sameDirection;
        if (sameDirection == 0 && Math.abs(diff) > 5.0f) {
            if (increaseBuffer() > maxBuffer()) fail("deltaPitch=" + diff);
        } else {
            resetBuffer();
        }
        lastDiff = diff;
    }
}
