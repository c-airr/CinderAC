package pl.airr.cinderac.check.player.badpackets;

import org.bukkit.GameMode;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'Z', complexType = "Spectate", description = "Invalid Spectate Packets.")
public final class BadPacketsZ extends Check {
    public BadPacketsZ(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isSpectate() || data.player == null) return;
        if (data.player.getGameMode() != GameMode.SPECTATOR && data.action.packetGameMode != GameMode.SPECTATOR) failNoInfo();
    }
}
