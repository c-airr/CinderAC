package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'W', complexType = "Analysis", description = "Generic rotation analysis heuristic.")
public final class AimW extends Check {
    public AimW(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        int sens = data.rotation.sensitivity;
        if (sens < -10 && dy > 1.25f && dp > 1.25f && !exempt(Exempt.CINEMATIC)) {
            if (increaseBuffer() > maxBuffer()) fail("sens=" + sens + " deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
