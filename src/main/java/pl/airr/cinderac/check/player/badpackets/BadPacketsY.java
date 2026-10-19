package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'Y', complexType = "Invalid Position", description = "NaN X/Y/Z values.")
public final class BadPacketsY extends Check {
    public BadPacketsY(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        double x = data.position.x, y = data.position.y, z = data.position.z;
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || x >= 2.147483647E9 || y >= 2.147483647E9 || z >= 2.147483647E9) {
            fail("x=" + x + " y=" + y + " z=" + z);
        }
    }
}
