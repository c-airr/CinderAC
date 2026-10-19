package pl.airr.cinderac.check.player.airplace;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Air Place", type = 'A', complexType = "Invalid", description = "Invalid block placement.")
public final class AirPlaceA extends Check {
    public AirPlaceA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        boolean exempt = exempt(Exempt.SPECTATOR, Exempt.CANCELLED_PLACE, Exempt.BLOCK_BREAK);
        if (e.isCancelled() || exempt) return;
        Material m = e.getBlock().getType();
        if (Blocks.SLAB.is(m) || Blocks.SAND.is(m) || Blocks.GRAVEL.is(m) || Blocks.ANVIL.is(m) || Blocks.TNT.is(m) || Blocks.PATH.is(m)
                || Blocks.ANCHOR.is(m) || Blocks.STRIPPED.is(m) || Blocks.FARMLAND.is(m)) {
            return;
        }
        Block b = e.getBlock(), against = e.getBlockAgainst();
        if (against.getX() != b.getX() || against.getY() != b.getY() || against.getZ() != b.getZ()) return;
        if (air(b, 0, 1, 0) && air(b, 0, -1, 0) && air(b, 1, 0, 0) && air(b, -1, 0, 0) && air(b, 0, 0, 1) && air(b, 0, 0, -1)) {
            fail("block=" + m);
        }
    }

    private static boolean air(Block b, int x, int y, int z) {
        return Blocks.isAir(b.getRelative(x, y, z).getType());
    }
}
