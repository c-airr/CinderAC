package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'L', complexType = "Kurtosis Difference", description = "Similar kurtosis values.")
public final class AutoClickerL extends ClickCheck {
    public AutoClickerL(PlayerData data) {
        super(data, 50, false);
    }

    private double lastKurtosis;

    @Override
    void evaluate(List<Long> s) {
        double kurtosis = VMath.kurtosis(s) / 1000.0, diff = Math.abs(kurtosis - lastKurtosis);
        if (diff < 8.35) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff);
        } else {
            decreaseBuffer();
        }
        lastKurtosis = kurtosis;
    }
}
