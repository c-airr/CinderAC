package pl.airr.cinderac.check.combat.autoblock;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Auto Block", type = 'D', complexType = "Order", description = "Invalid attack order.")
public final class AutoBlockD extends Check {
    public AutoBlockD(PlayerData data) {
        super(data);
    }

    private boolean attacked, interacted;

    @Override
    public void handle(Packet p) {
        if (p.isFlying()) {
            attacked = false;
            interacted = false;
        } else if (p.isAttack()) {
            WrapperPlayClientInteractEntity.InteractAction a = p.interact().getAction();
            if (a == WrapperPlayClientInteractEntity.InteractAction.ATTACK) attacked = true;
            if (a == WrapperPlayClientInteractEntity.InteractAction.INTERACT || a == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
                interacted = true;
            }
        } else if (p.isBlockPlace() && !exempt(Exempt.CLIENT_VERSION) && attacked && !interacted) {
            fail("attacked=" + attacked + " interacted=" + interacted);
        }
    }
}
