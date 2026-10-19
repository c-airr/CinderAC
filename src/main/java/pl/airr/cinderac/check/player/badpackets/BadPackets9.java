package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = '9', complexType = "No Swing", description = "Hitting without swinging.")
public final class BadPackets9 extends Check {
    public BadPackets9(PlayerData data) {
        super(data);
    }


    private boolean swung, attacked;

    @Override
    public void handle(Packet p) {
        if (data.clientVersion().isOlderThan(ClientVersion.V_1_21_2) || exempt(Exempt.SPECTATOR)) return;
        if (p.isAnimation()) {
            swung = true;
        } else if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
            attacked = true;
        } else if (p.isClientTickEnd()) {
            if (attacked && !swung) fail("attacked=" + attacked + " swung=" + swung);
            attacked = false;
            swung = false;
        }
    }
}
