package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'A', complexType = "Invalid", description = "Possible transaction disabler. Do not ban for this check.")
public final class InvalidA extends Check {
    public InvalidA(PlayerData data) {
        super(data);
    }

    private int streak;

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        streak = data.player != null && data.position.serverFlightAllowed && !data.player.getAllowFlight() ? streak + 1 : 0;
        if (streak > 100) failNoInfo();
    }
}
