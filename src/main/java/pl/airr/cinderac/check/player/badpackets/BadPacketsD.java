package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'D', complexType = "Timer", description = ".")
public final class BadPacketsD extends Check {
    public BadPacketsD(PlayerData data) {
        super(data);
    }


    private boolean moved;

    @Override
    public void handle(Packet p) {
        // shortcut: the original only requires a 1.12.2+ server, but without CLIENT_TICK_END (server < 1.21.2) it would flag everyone
        if (!ServerVersion.atLeast(21, 2) || data.clientVersion().isOlderThan(ClientVersion.V_1_12_2)) return;
        if (p.isFlying()) {
            boolean teleport = data.action.sinceServerPositionTicks < 5;
            if (moved && !teleport) fail("moved=" + moved);
            moved = true;
        } else if (p.isClientTickEnd()) {
            moved = false;
        }
    }
}
