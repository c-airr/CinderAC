package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'J', complexType = "Range", description = "Impossible consistency.")
public final class AutoClickerJ extends ClickCheck {
    public AutoClickerJ(PlayerData data) {
        super(data, 10, false);
    }

    @Override
    void evaluate(List<Long> s) {
        java.util.Collections.sort(s);
        long range = s.get(s.size() - 1) - s.get(0);
        if (range < 50L) {
            if (increaseBuffer() > maxBuffer()) fail("range=" + range);
        } else {
            decreaseBuffer();
        }
    }
}
