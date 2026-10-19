package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'D', complexType = "Skewness", description = "Too low skewness values.")
public final class AutoClickerD extends ClickCheck {
    public AutoClickerD(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        double skewness = VMath.skewness(s);
        if (skewness < -0.01) {
            if (increaseBuffer() > maxBuffer()) fail("skewness=" + skewness);
        } else {
            decreaseBuffer();
        }
    }
}
