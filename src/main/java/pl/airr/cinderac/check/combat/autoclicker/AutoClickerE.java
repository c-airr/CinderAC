package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'E', complexType = "Variance", description = "Too low variance.")
public final class AutoClickerE extends ClickCheck {
    public AutoClickerE(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        double variance = VMath.variance(s) / 1000.0;
        if (variance < 28.2) {
            if (increaseBuffer() > maxBuffer()) fail("variance=" + variance);
        } else {
            decreaseBuffer();
        }
    }
}
