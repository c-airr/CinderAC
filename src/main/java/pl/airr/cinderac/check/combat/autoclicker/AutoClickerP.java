package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'P', complexType = "Identical", description = "Identical statistical values.")
public final class AutoClickerP extends ClickCheck {
    public AutoClickerP(PlayerData data) {
        super(data, 15, false);
    }

    private double lastDeviation, lastSkewness, lastKurtosis;

    @Override
    void evaluate(List<Long> s) {
        double deviation = VMath.deviation(s), skewness = VMath.skewness(s), kurtosis = VMath.kurtosis(s);
        if (deviation == lastDeviation && skewness == lastSkewness && kurtosis == lastKurtosis) {
            if (increaseBuffer() > maxBuffer()) failNoInfo();
        } else {
            resetBuffer();
        }
        lastDeviation = deviation;
        lastSkewness = skewness;
        lastKurtosis = kurtosis;
    }
}
