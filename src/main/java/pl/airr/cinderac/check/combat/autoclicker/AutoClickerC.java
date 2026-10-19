package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'C', complexType = "Rounded", description = "Rounded CPS values.")
public final class AutoClickerC extends ClickCheck {
    public AutoClickerC(PlayerData data) {
        super(data, 20, false);
    }

    @Override
    void evaluate(List<Long> s) {
        double cps = VMath.cps(s), rounded = Math.abs((double) Math.round(cps) - cps);
        if (rounded < 0.08) {
            if (increaseBuffer() > maxBuffer()) fail("rounded=" + rounded);
        } else {
            decreaseBuffer();
        }
    }
}
