package pl.airr.cinderac.check.combat.velocity;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Velocity", type = 'C', complexType = "Ignored Vertical", description = "Vertical velocity modifications.")
public final class VelocityC extends Check {
    public VelocityC(PlayerData data) {
        super(data);
    }

    // empty in the original (no logic)
    @Override
    public void handle(Packet p) {
    }
}
