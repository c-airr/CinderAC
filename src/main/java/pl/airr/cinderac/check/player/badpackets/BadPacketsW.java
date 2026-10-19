package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'W', complexType = "Post WindowClick", description = "Post WindowClick packets.")
public final class BadPacketsW extends Check {
    public BadPacketsW(PlayerData data) {
        super(data);
    }


    // empty in the original (no logic)
    @Override
    public void handle(Packet p) {
    }
}
