package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = '7', complexType = "No Swing", description = "Hitting without swinging.")
public final class BadPackets7 extends Check {
    public BadPackets7(PlayerData data) {
        super(data);
    }


    private boolean lastSwung, swungEver;

    {
        lastSwung = data.clientVersion().isNewerThan(ClientVersion.V_1_8);
    }

    @Override
    public void handle(Packet p) {
        if (data.clientVersion().isOlderThan(ClientVersion.V_1_9) && ServerVersion.atLeast(9) || exempt(Exempt.SPECTATOR)) return;
        if (p.isAnimation()) {
            swungEver = true;
            lastSwung = true;
        }
        if (p.isAttack()) {
            if (p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                boolean newer = data.clientVersion().isNewerThan(ClientVersion.V_1_9);
                if (newer ? !lastSwung : !swungEver) fail("swung=" + swungEver);
            }
            lastSwung = swungEver;
        }
    }
}
