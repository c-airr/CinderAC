package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Scaffold", type = 'B', complexType = "Interact", description = "Invalid interact.")
public final class ScaffoldB extends Check {
    public ScaffoldB(PlayerData data) {
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
        if (e.isCancelled() || exempt(Exempt.CANCELLED_PLACE)) return;
        long delay = System.currentTimeMillis() - lastPlace;
        BlockFace face = e.getBlockAgainst().getFace(e.getBlock());
        Material m = e.getBlock().getType();
        boolean special = Blocks.TURTLE_EGG_LOOSE.is(m) || Blocks.SLAB.is(m) || Blocks.TNT.is(m) || Blocks.CONCRETE_POWDER.is(m);
        boolean exempt = exempt(Exempt.LIQUID, Exempt.CREATIVE, Exempt.FLIGHT, Exempt.TURTLE_EGG, Exempt.SLAB);
        boolean invalid = data.action.bridging && face == BlockFace.SELF && delay < 300L;
        if (invalid && !exempt && !special) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + delay + " face=" + face.toString().toLowerCase());
        } else {
            decreaseBuffer();
        }
        lastPlace = System.currentTimeMillis();
    }
}
