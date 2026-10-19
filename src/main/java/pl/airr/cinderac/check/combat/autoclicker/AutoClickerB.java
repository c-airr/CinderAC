package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'B', complexType = "Deviation", description = "Too low standard deviation.")
public final class AutoClickerB extends ClickCheck {
    public AutoClickerB(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        double deviation = VMath.deviation(s), average = VMath.mean(s);
        if (deviation < 167.0) {
            if (increaseBuffer() > maxBuffer()) fail("deviation=" + deviation + " average=" + average);
            else decreaseBuffer();
        }
    }
}
