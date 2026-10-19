package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'S', complexType = "Distinct", description = "Checks for amount of distinct delays.")
public final class AutoClickerS extends ClickCheck {
    public AutoClickerS(PlayerData data) {
        super(data, 25, true);
    }

    @Override
    void evaluate(List<Long> s) {
        int distinct = VMath.distinct(s);
        if (distinct < 6) {
            if (increaseBuffer() > maxBuffer()) fail("distinct=" + distinct);
        } else {
            decreaseBuffer();
        }
    }
}
