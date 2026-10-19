package pl.airr.cinderac.check.player.scaffold;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;

import java.util.HashMap;
import java.util.Map;

@CheckInfo(name = "Scaffold", type = 'K', complexType = "Limit", description = "Bridging too quickly.")
public final class ScaffoldK extends Check {
    public ScaffoldK(PlayerData data) {
        super(data);
    }

    private final Map<Integer, Long> placed = new HashMap<>();
    private int lastSneakTick, count;

    @Override
    public void handle(Packet p) {
        if (!p.isEntityAction()) return;
        WrapperPlayClientEntityAction.Action a = p.entityAction().getAction();
        if (a == WrapperPlayClientEntityAction.Action.START_SNEAKING || a == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) lastSneakTick = tick();
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        if (e.isCancelled() || Blocks.SLAB.is(e.getBlock().getType()) || exempt(Exempt.SLAB)) return;
        if (ServerVersion.atLeast(19) && data.action.swiftSneak) return;
        if (!data.action.bridging || tick() - lastSneakTick <= 20) return;
        placed.put(++count, System.currentTimeMillis());
        if (System.currentTimeMillis() - placed.get(1) > 1000L) {
            int n = placed.size();
            if (n >= cfg.getInt("max-blocks", 8)) {
                if (increaseBuffer() > maxBuffer()) fail("blocks=" + n);
            } else {
                decreaseBuffer();
            }
            placed.clear();
            count = 0;
        }
    }
}
