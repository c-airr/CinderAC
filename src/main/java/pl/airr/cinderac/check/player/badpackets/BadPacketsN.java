package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'N', complexType = "Post BlockDig", description = "Post BlockDig packets.")
public final class BadPacketsN extends PostPacketCheck {
    public BadPacketsN(PlayerData data) {
        super(data);
    }


    @Override
    boolean isTarget(Packet p) {
        return p.isDigging();
    }
}
