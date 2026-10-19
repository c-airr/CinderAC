package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'H', complexType = "Average Deviation", description = "Similar deviation values.")
public final class AutoClickerH extends ClickCheck {
    public AutoClickerH(PlayerData data) {
        super(data, 20, false);
    }

    private double lastDeviation;

    @Override
    void evaluate(List<Long> s) {
        double deviation = VMath.deviation(s), diff = Math.abs(deviation - lastDeviation);
        if (diff < 7.52) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff);
        } else {
            decreaseBuffer();
        }
        lastDeviation = deviation;
    }
}
