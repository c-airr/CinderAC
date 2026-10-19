package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'G', complexType = "Hotbar", description = "Invalid HeldItemSlot packet.")
public final class BadPacketsG extends Check {
    public BadPacketsG(PlayerData data) {
        super(data);
    }


    private int lastSlot;

    @Override
    public void handle(Packet p) {
        if (!p.isHeldItemChange()) return;
        int slot = p.heldItemChange().getSlot();
        if (slot == lastSlot) fail("slot=" + slot + " lastSlot=" + lastSlot);
        lastSlot = slot;
    }
}
