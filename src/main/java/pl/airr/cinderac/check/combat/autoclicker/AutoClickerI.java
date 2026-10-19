package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'I', complexType = "Kurtosis", description = "Too low kurtosis.")
public final class AutoClickerI extends ClickCheck {
    public AutoClickerI(PlayerData data) {
        super(data, 50, false);
    }

    @Override
    void evaluate(List<Long> s) {
        double kurtosis = VMath.kurtosis(s) / 1000.0;
        if (kurtosis < 41.78) {
            if (increaseBuffer() > maxBuffer()) fail("kurtosis=" + kurtosis);
        } else {
            decreaseBuffer();
        }
    }
}
