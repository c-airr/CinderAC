package pl.airr.cinderac.check.player.badpackets;

import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'P', complexType = "Post Entity Action", description = "Post EntityAction packets.")
public final class BadPacketsP extends PostPacketCheck {
    public BadPacketsP(PlayerData data) {
        super(data);
    }


    @Override
    boolean isTarget(Packet p) {
        return p.isEntityAction();
    }

    @Override
    boolean skip() {
        return ServerVersion.atLeast(9) || exempt(Exempt.CREATIVE, Exempt.SPECTATOR);
    }

    @Override
    void flag(long delay) {
        failNoInfo();
    }
}
