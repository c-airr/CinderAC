package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Scaffold", type = 'C', complexType = "Sprint", description = "Sprinting while bridging.")
public final class ScaffoldC extends Check {
    public ScaffoldC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || ((BlockPlaceEvent) event).isCancelled()) return;
        boolean bridging = data.action.sinceBridgingTicks < 1;
        // in the original the time since the previous bridge is never set (always 0)
        long delay = 0L;
        int sprint = data.action.sprintingTicks;
        boolean exempt = exempt(Exempt.LIQUID, Exempt.CREATIVE, Exempt.FLIGHT);
        if (bridging && delay < 300L && sprint > 5 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("ticks=" + sprint + " delay=" + delay);
        } else {
            decreaseBuffer();
        }
    }
}
