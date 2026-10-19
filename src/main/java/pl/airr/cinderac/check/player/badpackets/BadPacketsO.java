package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'O', complexType = "N/A", description = "Negative HeldItemSlot packet.")
public final class BadPacketsO extends Check {
    public BadPacketsO(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (p.isHeldItemChange() && p.heldItemChange().getSlot() < 0) fail("slot=" + p.heldItemChange().getSlot());
    }
}
