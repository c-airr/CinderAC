package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'O', complexType = "Spikes", description = "Impossible spike in CPS.")
public final class AutoClickerO extends ClickCheck {
    public AutoClickerO(PlayerData data) {
        super(data, 10, false);
    }

    private double lastCps;

    @Override
    void evaluate(List<Long> s) {
        double cps = VMath.cps(s);
        if (lastCps > 0.0) {
            double diff = Math.abs(cps - lastCps), average = (lastCps + cps) / 2.0;
            if (average > 9.25 && diff > 2.8) {
                if (increaseBuffer() > maxBuffer()) fail("average=" + average + " difference=" + diff);
            } else {
                decreaseBuffer();
            }
        }
        lastCps = cps;
    }
}
