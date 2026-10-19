package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Scaffold", type = 'I', complexType = "Acceleration", description = "Invalid acceleration.")
public final class ScaffoldI extends Check {
    public ScaffoldI(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || ((BlockPlaceEvent) event).isCancelled() || !data.action.bridging) return;
        double accel = data.position.accelXZ();
        float dp = data.rotation.deltaPitch, dy = data.rotation.deltaYaw;
        if (accel < 0.01 && dy > 150.0f && dp > 10.0f) fail("acceleration=" + accel + " deltaYaw=" + dy + " deltaPitch=" + dp);
    }
}
