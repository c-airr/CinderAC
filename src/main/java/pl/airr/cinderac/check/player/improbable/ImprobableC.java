package pl.airr.cinderac.check.player.improbable;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'C', complexType = "Player", description = "Too many combined player violations.")
public final class ImprobableC extends Check {
    public ImprobableC(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isPosition())) return;
        int n = data.playerViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-player-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
