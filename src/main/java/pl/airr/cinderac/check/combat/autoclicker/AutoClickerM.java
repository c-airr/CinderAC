package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'M', complexType = "Variance Difference", description = "Similar variance values.")
public final class AutoClickerM extends ClickCheck {
    public AutoClickerM(PlayerData data) {
        super(data, 50, false);
    }

    private double lastVariance;

    @Override
    void evaluate(List<Long> s) {
        double variance = VMath.variance(s) / 1000.0, diff = Math.abs(variance - lastVariance);
        if (diff < 5.28) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff);
        } else {
            decreaseBuffer();
        }
        lastVariance = variance;
    }
}
