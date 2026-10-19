package pl.airr.cinderac.check.player.improbable;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'E', complexType = "Total", description = "Too many combined violations.")
public final class ImprobableE extends Check {
    public ImprobableE(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isFlying())) return;
        int n = data.totalViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-total-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
