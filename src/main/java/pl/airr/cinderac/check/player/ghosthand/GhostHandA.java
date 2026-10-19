package pl.airr.cinderac.check.player.ghosthand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Ghost Hand", type = 'A', complexType = "Bed", description = "Invalid bed break.")
public final class GhostHandA extends Check {
    public GhostHandA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockBreakEvent)) return;
        BlockBreakEvent e = (BlockBreakEvent) event;
        Block bed = e.getBlock();
        String type = bed.getType().name();
        if (!type.contains("BED") || type.equalsIgnoreCase("BEDROCK")) return;
        if (data.position.y < bed.getY()) return;
        // other half of the bed; order as in the original (last match wins)
        int dx = 0, dz = 0;
        BlockFace face = null;
        if (isBed(bed, 0, 0, 1)) { face = BlockFace.SOUTH; dx = 0; dz = 1; }
        if (isBed(bed, -1, 0, 0)) { face = BlockFace.WEST; dx = -1; dz = 0; }
        if (isBed(bed, 0, 0, -1)) { face = BlockFace.NORTH; dx = 0; dz = -1; }
        if (isBed(bed, 1, 0, 0)) { face = BlockFace.EAST; dx = 1; dz = 0; }
        if (face == null) return;
        int lx = Math.abs(dz), lz = Math.abs(dx);
        List<Material> around = new ArrayList<>();
        around.add(type(bed, 0, -1, 0));
        around.add(type(bed, dx, -1, dz));
        around.add(type(bed, -dx, 0, -dz));
        around.add(type(bed, -lx, 0, -lz));
        around.add(type(bed, lx, 0, lz));
        around.add(type(bed, dx - lx, 0, dz - lz));
        around.add(type(bed, dx + lx, 0, dz + lz));
        around.add(type(bed, 2 * dx, 0, 2 * dz));
        around.add(type(bed, 0, 1, 0));
        around.add(type(bed, dx, 1, dz));
        boolean closed = true;
        for (Material m : around) closed &= m.isSolid() && m.isOccluding();
        around.remove(0);
        around.remove(0);
        boolean glass = Blocks.all(around, Blocks.GLASS);
        if (closed || glass) {
            fail("direction=" + face + " blocks=" + around);
            if (cfg.getBoolean("cancel", false)) e.setCancelled(true);
        }
    }

    private static boolean isBed(Block b, int x, int y, int z) {
        return type(b, x, y, z).name().contains("BED");
    }

    private static Material type(Block b, int x, int y, int z) {
        return b.getRelative(x, y, z).getType();
    }
}
