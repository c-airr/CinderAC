package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'K', complexType = "Average Difference", description = "Similar average values.")
public final class AutoClickerK extends ClickCheck {
    public AutoClickerK(PlayerData data) {
        super(data, 50, false);
    }

    private double lastAverage;

    @Override
    void evaluate(List<Long> s) {
        double average = VMath.mean(s), diff = Math.abs(average - lastAverage);
        if (diff < 2.56) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff);
        } else {
            decreaseBuffer();
        }
        lastAverage = average;
    }
}
