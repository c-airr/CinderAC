package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Scaffold", type = 'A', complexType = "Interact", description = "Interacted with the bottom of block.")
public final class ScaffoldA extends Check {
    public ScaffoldA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof PlayerInteractEvent)) return;
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (System.currentTimeMillis() - data.clicks.lastItemFrameInteract < 10000L || data.player == null) return;
        Block below = data.player.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.SERVER_POSITION);
        Block clicked = e.getClickedBlock();
        if (clicked == null || !clicked.equals(below) || e.getBlockFace() != BlockFace.DOWN) return;
        if (!clicked.getType().isSolid() || !data.player.getItemInHand().getType().isSolid() || exempt) return;
        fail("block=" + clicked.getType());
    }
}
