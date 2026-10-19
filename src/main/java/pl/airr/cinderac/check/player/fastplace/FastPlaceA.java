package pl.airr.cinderac.check.player.fastplace;

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

@CheckInfo(name = "Fast Place", type = 'A', complexType = "Delay", description = "Placing blocks too quickly.")
public final class FastPlaceA extends Check {
    public FastPlaceA(PlayerData data) {
        super(data);
    }

    private final Map<Integer, Long> placed = new HashMap<>();
    private int count;

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || data.player == null) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        boolean exempt = exempt(Exempt.CANCELLED_BREAK, Exempt.VEHICLE) || e.isCancelled();
        if (data.player.getItemInHand().getType().name().contains("HOE")) return;
        if (ServerVersion.atLeast(9) && data.player.getInventory().getItemInOffHand().getType().name().contains("HOE")) return;
        if (exempt || Blocks.SCAFFOLDING.is(e.getBlock().getType()) || Blocks.SCULK.is(e.getBlock().getType())) return;
        placed.put(++count, System.currentTimeMillis());
        if (System.currentTimeMillis() - placed.get(1) > 1000L) {
            if (placed.size() > cfg.getInt("max-blocks", 20)) fail("amount=" + placed.size());
            placed.clear();
            count = 0;
        }
    }
}
