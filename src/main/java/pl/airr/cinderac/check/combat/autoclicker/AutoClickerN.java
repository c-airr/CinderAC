package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'N', complexType = "Deviation Difference", description = "Low deviation difference.")
public final class AutoClickerN extends ClickCheck {
    public AutoClickerN(PlayerData data) {
        super(data, 25, true);
    }

    private double lastDeviation;

    @Override
    void evaluate(List<Long> s) {
        double deviation = VMath.deviation(s), diff = Math.abs(deviation - lastDeviation), average = Math.abs(deviation + lastDeviation) / 2.0;
        if (diff < 0.25 && average < 150.0) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff + " average=" + average);
        } else {
            decreaseBuffer();
        }
        lastDeviation = deviation;
    }
}
