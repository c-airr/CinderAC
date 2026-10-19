package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'F', complexType = "Distinct", description = "Not enough distinct values.")
public final class AutoClickerF extends ClickCheck {
    public AutoClickerF(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        int distinct = VMath.distinct(s);
        if (distinct < 13) {
            if (increaseBuffer() > maxBuffer()) fail("distinct=" + distinct);
        } else {
            decreaseBuffer();
        }
    }
}
