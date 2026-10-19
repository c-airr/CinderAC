package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Scaffold", type = 'J', complexType = "Acceleration [2]", description = "Invalid pitch change.")
public final class ScaffoldJ extends Check {
    public ScaffoldJ(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || !data.action.bridging) return;
        double accel = data.position.accelXZ();
        float dp = data.rotation.deltaPitch;
        if (VMath.isScientific(accel) && dp > 18.0f) fail("acceleration=" + accel + " deltaPitch=" + dp);
    }
}
