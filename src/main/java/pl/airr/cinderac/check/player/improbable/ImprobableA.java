package pl.airr.cinderac.check.player.improbable;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Improbable", type = 'A', complexType = "Combat", description = "Too many combined combat violations.")
public final class ImprobableA extends Check {
    public ImprobableA(PlayerData data) {
        super(data);
    }

    private long lastFlag;

    @Override
    public void handle(Packet p) {
        if (!(p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;
        int n = data.combatViolations;
        if (System.currentTimeMillis() - lastFlag >= 60000L && n > cfg.getInt("max-combat-violations", 100)) {
            fail("violations=" + n);
            lastFlag = System.currentTimeMillis();
        }
    }
}
