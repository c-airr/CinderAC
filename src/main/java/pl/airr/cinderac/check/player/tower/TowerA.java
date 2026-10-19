package pl.airr.cinderac.check.player.tower;

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

@CheckInfo(name = "Tower", type = 'A', complexType = "Limit", description = "Towering too quickly.")
public final class TowerA extends Check {
    public TowerA(PlayerData data) {
        super(data);
    }

    private long lastPlace;

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || data.player == null) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        if (e.isCancelled() || !e.getBlock().getType().isSolid()) return;
        long delay = System.currentTimeMillis() - lastPlace;
        double dxz = data.position.deltaXZ, dy = data.position.deltaY;
        boolean sameX = data.player.getLocation().getBlockX() == e.getBlock().getLocation().getBlockX();
        boolean sameZ = data.player.getLocation().getBlockZ() == e.getBlock().getLocation().getBlockZ();
        BlockFace face = e.getBlockAgainst().getFace(e.getBlock());
        Material m = e.getBlock().getType();
        boolean special = Blocks.SCAFFOLDING.is(m) || Blocks.WIRE.is(m) || Blocks.TURTLE_EGG_LOOSE.is(m) || Blocks.CLIMBABLE_LOOSE.is(m);
        boolean exempt = exempt(Exempt.CREATIVE, Exempt.FLIGHT, Exempt.JUMP_BOOST, Exempt.CANCELLED_PLACE, Exempt.LIQUID, Exempt.TURTLE_EGG,
                Exempt.CLIMBABLE, Exempt.EXPLOSION, Exempt.ELYTRA, Exempt.PACKET_EXPLOSION);
        boolean tower = face == BlockFace.UP && sameX && sameZ && delay < 250L && dy > 0.0 && dxz < 0.1;
        if (tower && !exempt && !special) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + delay + " deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
        lastPlace = System.currentTimeMillis();
    }
}
