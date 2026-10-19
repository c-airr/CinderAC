package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'R', complexType = "Consistency", description = "Impossible consistency.")
public final class AutoClickerR extends ClickCheck {
    public AutoClickerR(PlayerData data) {
        super(data, 30, true);
    }

    @Override
    void evaluate(List<Long> s) {
        int outliers = 0;
        for (long d : s) if (d > 150L) outliers++;
        double average = VMath.mean(s);
        if (outliers == 0) {
            if (increaseBuffer() > maxBuffer()) fail("outliers=" + outliers + " average=" + average);
        } else {
            decreaseBuffer();
        }
    }
}
