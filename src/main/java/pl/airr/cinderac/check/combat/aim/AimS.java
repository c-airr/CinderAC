package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'S', complexType = "Divisor Y", description = "Subtle aim assist modifications.")
public final class AimS extends Check {
    public AimS(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3 || !data.rotation.validSensitivity()) return;
        double sens = data.rotation.sensitivityValue;
        if (sens < 0.01) return;
        float f = (float) sens * 0.6f + 0.2f;
        float step = f * f * f * 1.2f;
        if ((double) step < 1.0E-4) return;
        double x = data.rotation.deltaYaw / step, y = data.rotation.deltaPitch / step;
        double divX = Math.abs((double) Math.round(x) - x), divY = Math.abs((double) Math.round(y) - y);
        if (divY > 0.03 && divX < 1.0E-4) {
            if (increaseBuffer() > maxBuffer()) fail("divisorX=" + divX + " divisorY=" + divY);
        } else {
            decreaseBuffer();
        }
    }
}
