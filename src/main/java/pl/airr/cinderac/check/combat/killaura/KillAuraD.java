package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'D', complexType = "Multi Aura", description = "Attacked two entities at once.")
public final class KillAuraD extends Check {
    public KillAuraD(PlayerData data) {
        super(data);
    }

    private int lastId, targets;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
            boolean exempt = exempt(Exempt.CLIENT_VERSION, Exempt.SERVER_VERSION);
            int id = p.interact().getEntityId();
            if (id != lastId && !exempt && ++targets > 1) fail("id=" + id + " lastId=" + lastId);
            lastId = id;
        } else if (p.isFlying()) {
            targets = 0;
        }
    }
}
