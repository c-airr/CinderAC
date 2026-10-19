package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'C', complexType = "Pitch", description = "Impossible pitch.")
public final class BadPacketsC extends Check {
    public BadPacketsC(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (p.isRotation() && Math.abs(data.rotation.pitch) > 90.0f) fail("pitch=" + data.rotation.pitch);
    }
}
