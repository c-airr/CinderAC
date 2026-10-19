package pl.airr.cinderac.check.combat.autoblock;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Auto Block", type = 'B', complexType = "Packet", description = "Attacked while sending BlockDig packet.")
public final class AutoBlockB extends Check {
    public AutoBlockB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!(p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;
        if (data.action.releasedUseItemThisTick && !exempt(Exempt.CLIENT_VERSION)) failNoInfo();
    }
}
