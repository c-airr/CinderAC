package pl.airr.cinderac.check.player.improbable;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'F', complexType = "Scaffold", description = "Too many combined Scaffold violations.")
public final class ImprobableF extends Check {
    public ImprobableF(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isPosition())) return;
        int n = data.scaffoldViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-scaffold-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
