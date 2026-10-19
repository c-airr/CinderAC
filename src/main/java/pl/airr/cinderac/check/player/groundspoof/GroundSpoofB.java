package pl.airr.cinderac.check.player.groundspoof;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Ground Spoof", type = 'B', complexType = "Spoof", description = "Spoofed OnGround value.")
public final class GroundSpoofB extends Check {
    public GroundSpoofB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        boolean math = data.position.mathematicallyOnGround;
        boolean ground = data.position.onGround;
        boolean air = data.position.allAir;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.CREATIVE, Exempt.BOAT, Exempt.TELEPORT, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.CHUNK,
                Exempt.CARPET, Exempt.VEHICLE, Exempt.SCAFFOLDING, Exempt.DEAD, Exempt.HAPPY_GHAST, Exempt.GLIDING);
        double y = data.position.y;
        boolean special = y % 1.0 == 0.6 || y % 0.1 == 0.5625;
        if (ground && !math && !exempt && air && !special) {
            if (increaseBuffer() > maxBuffer()) fail("client=" + ground + " server=" + math);
        } else {
            decreaseBuffer();
        }
    }
}
