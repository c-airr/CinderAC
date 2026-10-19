package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'G', complexType = "Outliers", description = "Too low outliers.")
public final class AutoClickerG extends ClickCheck {
    public AutoClickerG(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        int outliers = 0;
        for (long d : s) if (d > 150L) outliers++;
        double average = VMath.mean(s);
        if (outliers < 3) {
            if (increaseBuffer() > maxBuffer()) fail("outliers=" + outliers + " average=" + average);
        } else {
            decreaseBuffer();
        }
    }
}
