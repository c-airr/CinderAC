package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'E', complexType = "Interact", description = "Interacted with themselves.")
public final class BadPacketsE extends Check {
    public BadPacketsE(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (p.isAttack() && p.interact().getEntityId() == data.entityId && !exempt(Exempt.SPECTATOR)) failNoInfo();
    }
}
