package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'J', complexType = "Held Item Slot", description = "Sent HeldItemSlot while placing.")
public final class BadPacketsJ extends Check {
    public BadPacketsJ(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isHeldItemChange()) return;
        boolean exempt = exempt(Exempt.CLIENT_VERSION, Exempt.SERVER_VERSION, Exempt.CREATIVE, Exempt.SPECTATOR);
        if (!exempt && data.action.placedThisTick) failNoInfo();
    }
}
