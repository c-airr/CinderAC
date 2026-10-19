package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'B', complexType = "Invalid", description = "Spamming ghost block setbacks. Do not ban for this check.")
public final class InvalidB extends Check {
    public InvalidB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (p.isPosition() && data.position.ghostBlockSetbacks > 5) failNoInfo();
    }
}
