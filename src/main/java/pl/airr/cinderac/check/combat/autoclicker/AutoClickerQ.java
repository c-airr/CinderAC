package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'Q', complexType = "Average Deviation", description = "Too low average deviation.")
public final class AutoClickerQ extends ClickCheck {
    public AutoClickerQ(PlayerData data) {
        super(data, 40, true);
    }

    private double lastDeviation;

    @Override
    void evaluate(List<Long> s) {
        double deviation = VMath.deviation(s), diff = Math.abs(deviation - lastDeviation), average = Math.abs(deviation + lastDeviation) / 2.0;
        if (diff < 3.0 && average < 150.0) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff + " average=" + average);
        } else {
            decreaseBuffer();
        }
        lastDeviation = deviation;
    }
}
