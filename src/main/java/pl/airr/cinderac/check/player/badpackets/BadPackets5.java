package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.util.Vector3f;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = '5', complexType = "Scaffold", description = "Invalid Block place.")
public final class BadPackets5 extends Check {
    public BadPackets5(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isBlockPlace()) return;
        Vector3f c = p.blockPlace().getCursorPosition();
        if (exempt(Exempt.CREATIVE, Exempt.CLIENT_VERSION) || c == null) return;
        float x = c.getX(), y = c.getY(), z = c.getZ();
        if (x > 1.0f || y > 1.0f || z > 1.0f || x < 0.0f || y < 0.0f || z < 0.0f) fail("x=" + x + " y=" + y + " z=" + z);
    }
}
