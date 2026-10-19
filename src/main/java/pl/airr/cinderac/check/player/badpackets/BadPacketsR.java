package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'R', complexType = "Post HeldItemSlot", description = "Post HeldItemSlot packets.")
public final class BadPacketsR extends PostPacketCheck {
    public BadPacketsR(PlayerData data) {
        super(data);
    }


    @Override
    boolean isTarget(Packet p) {
        return p.isHeldItemChange();
    }

    @Override
    boolean exempt() {
        return exempt(Exempt.CREATIVE, Exempt.SPECTATOR);
    }
}
