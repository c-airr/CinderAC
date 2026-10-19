package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;
import pl.airr.cinderac.util.VMath;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Scaffold", type = 'N', complexType = "Expand", description = "Invalid block face.", experimental = true)
public final class ScaffoldN extends Check {
    public ScaffoldN(PlayerData data) {
        super(data);
    }

    private final List<Block> placed = new ArrayList<>();

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        Material m = e.getBlock().getType();
        if (e.isCancelled() || Blocks.SCAFFOLDING.is(m) || Blocks.SLAB.is(m) || Blocks.STAIRS.is(m)) return;
        Block block = e.getBlock();
        if (placed.contains(e.getBlockAgainst())) {
            boolean below = (double) e.getBlockPlaced().getY() < data.position.y;
            BlockFace face = e.getBlockAgainst().getFace(e.getBlock());
            Material under = data.position.below2;
            if (under == null || face == null) return;
            if (face == BlockFace.DOWN || face == BlockFace.UP || face == BlockFace.SELF) return;
            double dist = VMath.hypot(Math.abs(data.position.x - e.getBlockPlaced().getX()), Math.abs(data.position.z - e.getBlockPlaced().getZ()));
            boolean aligned = block.getX() == data.position.blockX || block.getZ() == data.position.blockZ;
            boolean expand = Blocks.isAir(under) && below && dist > 1.85 && aligned;
            if (expand && !exempt(Exempt.FLIGHT, Exempt.CREATIVE)) {
                if (data.player == null) return;
                // ray from the feet in the look direction, 1 block steps, up to 3 blocks
                Vector origin = data.player.getLocation().toVector();
                Vector dir = direction(data.rotation.yaw, data.rotation.pitch);
                int solid = 0;
                for (double t = 0.0; t <= 3.0; t += 1.0) {
                    if (origin.clone().add(dir.clone().multiply(t)).toLocation(data.player.getWorld()).getBlock().getType().isSolid()) solid++;
                }
                if (solid > 0) {
                    if (increaseBuffer() > maxBuffer()) fail("distanceXZ=" + dist + " r=" + solid);
                } else {
                    decreaseBuffer();
                }
            }
            placed.clear();
        }
        placed.add(block);
    }

    private static Vector direction(float yaw, float pitch) {
        Vector v = new Vector();
        v.setY(-Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        v.setX(-xz * Math.sin(Math.toRadians(yaw)));
        v.setZ(xz * Math.cos(Math.toRadians(yaw)));
        return v;
    }
}
