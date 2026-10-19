package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'I', complexType = "Entity Action", description = "Sent EntityAction while attacking.")
public final class BadPacketsI extends Check {
    public BadPacketsI(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isAttack()) return;
        boolean exempt = exempt(Exempt.SERVER_VERSION, Exempt.CLIENT_VERSION, Exempt.FAST, Exempt.SPECTATOR, Exempt.DEATH);
        if (data.action.sentEntityActionThisTick && !exempt) failNoInfo();
    }
}
