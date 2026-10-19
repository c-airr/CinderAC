package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'Q', complexType = "Hotbar", description = "Too big HeldItemSlot packet.")
public final class BadPacketsQ extends Check {
    public BadPacketsQ(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (p.isHeldItemChange() && p.heldItemChange().getSlot() > 8) fail("slot=" + p.heldItemChange().getSlot());
    }
}
