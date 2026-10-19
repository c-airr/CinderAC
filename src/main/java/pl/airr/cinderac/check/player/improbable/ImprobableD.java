package pl.airr.cinderac.check.player.improbable;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'D', complexType = "Auto Clicker", description = "Too many combined autoclicker violations.")
public final class ImprobableD extends Check {
    public ImprobableD(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isAnimation() && !exempt(Exempt.AUTOCLICKER))) return;
        int n = data.autoClickerViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-autoclicker-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
