package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'B', complexType = "Sequence", description = "More than 20 flying packets in a row.")
public final class BadPacketsB extends Check {
    public BadPacketsB(PlayerData data) {
        super(data);
    }


    private int streak;

    @Override
    public void handle(Packet p) {
        if (p.isFlying()) {
            if (p.flying().hasPositionChanged() || data.player != null && data.player.isInsideVehicle()) {
                streak = 0;
                return;
            }
            boolean exempt = exempt(Exempt.SERVER_VERSION, Exempt.CLIENT_VERSION);
            if (++streak > 20 && !exempt) fail("streak=" + streak);
        } else if (p.isSteerVehicle()) {
            streak = 0;
        }
    }
}
