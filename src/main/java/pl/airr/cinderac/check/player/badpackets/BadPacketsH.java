package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'H', complexType = "Packet Order", description = "Invalid attack packet order.")
public final class BadPacketsH extends Check {
    public BadPacketsH(PlayerData data) {
        super(data);
    }


    private boolean swung;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            boolean exempt = exempt(Exempt.SERVER_VERSION, Exempt.CLIENT_VERSION, Exempt.FAST);
            if (p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK && !exempt && !swung) fail("swung=" + swung);
        } else if (p.isAnimation()) {
            swung = true;
        } else if (p.isFlying()) {
            swung = false;
        }
    }
}
