package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Scaffold", type = 'M', complexType = "Expand", description = "Invalid block face.")
public final class ScaffoldM extends Check {
    public ScaffoldM(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        if (e.isCancelled() || Blocks.SCAFFOLDING.is(e.getBlock().getType())) return;
        boolean below = (double) e.getBlockPlaced().getY() < data.position.y;
        double dist = VMath.hypot(Math.abs(data.position.x - e.getBlockPlaced().getX()), Math.abs(data.position.z - e.getBlockPlaced().getZ()));
        BlockFace face = e.getBlockAgainst().getFace(e.getBlock());
        BlockFace dir = facing(data.rotation.yaw);
        if (below && dist > 1.0 && dist < 6.0 && face == dir) {
            if (increaseBuffer() > maxBuffer()) fail("[1] face=" + face + " direction=" + dir + " distance=" + dist);
        } else {
            decreaseBuffer();
        }
    }

    /** Cardinal direction the player is facing (yaw). */
    static BlockFace facing(float yaw) {
        float f = (yaw - 90.0f) % 360.0f;
        if (f < 0.0f) f = (float) ((double) f + 360.0);
        if (0.0f <= f && f < 45.0f) return BlockFace.WEST;
        if (45.0f <= f && f < 135.0f) return BlockFace.NORTH;
        if (135.0f <= f && f < 225.0f) return BlockFace.EAST;
        if (225.0f <= f && f < 315.0f) return BlockFace.SOUTH;
        if (315.0f <= f && (double) f < 360.0) return BlockFace.WEST;
        return BlockFace.SELF;
    }
}
