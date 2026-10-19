package pl.airr.cinderac.check.player.improbable;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'B', complexType = "Movement", description = "Too many combined movement violations.")
public final class ImprobableB extends Check {
    public ImprobableB(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isPosition())) return;
        int n = data.movementViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-movement-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
