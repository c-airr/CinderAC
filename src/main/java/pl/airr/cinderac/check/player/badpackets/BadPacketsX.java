package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'X', complexType = "Post ArmAnimation", description = "Post ArmAnimation packets.")
public final class BadPacketsX extends PostPacketCheck {
    public BadPacketsX(PlayerData data) {
        super(data);
    }


    @Override
    boolean isTarget(Packet p) {
        return p.isAnimation();
    }

    @Override
    boolean skip() {
        return exempt(Exempt.CLIENT_VERSION, Exempt.SERVER_VERSION);
    }

    @Override
    void flag(long delay) {
        failNoInfo();
    }
}
