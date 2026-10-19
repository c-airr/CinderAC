package pl.airr.cinderac.check.combat.autoblock;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Auto Block", type = 'C', complexType = "Order", description = "Attacked while sending BlockDig packet.")
public final class AutoBlockC extends Check {
    public AutoBlockC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!(p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) || data.player == null) return;
        boolean sword = data.player.getItemInHand().getType().name().contains("SWORD");
        if (data.action.diggingThisTick && sword && !exempt(Exempt.CLIENT_VERSION)) {
            if (increaseBuffer() > maxBuffer()) failNoInfo();
        } else {
            decreaseBuffer();
        }
    }
}
