package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Scaffold", type = 'E', complexType = "Rotations [2]", description = "Invalid rotations.")
public final class ScaffoldE extends Check {
    public ScaffoldE(PlayerData data) {
        super(data);
    }

    private long lastPlace, placeDelay;
    private boolean bridging;
    private int flyings;

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || ((BlockPlaceEvent) event).isCancelled()) return;
        bridging = data.action.bridging;
        placeDelay = System.currentTimeMillis() - lastPlace;
        flyings = 0;
        lastPlace = System.currentTimeMillis();
    }

    @Override
    public void handle(Packet p) {
        if (!p.isFlying() || ++flyings >= 3 || !bridging || placeDelay >= 400L) return;
        float dp = data.rotation.deltaPitch, dy = data.rotation.deltaYaw;
        if (dp > 8.0f && dy > 100.0f) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
