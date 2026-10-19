package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'F', complexType = "Spoofed Y", description = "Impossible Y-axis change.")
public final class InvalidF extends Check {
    public InvalidF(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        double dy = data.position.deltaY;
        boolean exempt = exempt(Exempt.DEATH, Exempt.JOINED, Exempt.TELEPORT, Exempt.WORLD_CHANGE);
        if (Math.abs(dy) > 100.0 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
