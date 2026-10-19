package pl.airr.cinderac.check.combat.autoblock;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Auto Block", type = 'A', complexType = "Sequence", description = "Attacked while sending BlockPlace packet.")
public final class AutoBlockA extends Check {
    public AutoBlockA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!(p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;
        if (data.action.placedThisTick && !exempt(Exempt.CLIENT_VERSION)) failNoInfo();
    }
}
