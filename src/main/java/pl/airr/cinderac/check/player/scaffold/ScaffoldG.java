package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Scaffold", type = 'G', complexType = "Speed", description = "Bridging too quickly.")
public final class ScaffoldG extends Check {
    public ScaffoldG(PlayerData data) {
        super(data);
    }

    private long lastPlace;

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        if (e.isCancelled() || Blocks.SLAB.is(e.getBlock().getType()) || data.action.swiftSneak || exempt(Exempt.SLAB)) return;
        if (!data.action.bridging) return;
        long delay = System.currentTimeMillis() - lastPlace;
        float angle = data.rotation.moveAngle();
        double accel = data.position.accelXZ();
        boolean scaffolding = Blocks.SCAFFOLDING.is(e.getBlock().getType());
        if (accel < 0.001 && accel > 0.0 && delay < 300L && angle < 2.0f && !scaffolding) {
            if (increaseBuffer() > maxBuffer()) fail("angle=" + angle + " delay=" + delay + " acceleration=" + accel);
        } else {
            decreaseBuffer();
        }
        lastPlace = System.currentTimeMillis();
    }
}
