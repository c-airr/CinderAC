package pl.airr.cinderac.check.player.scaffold;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Scaffold", type = 'F', complexType = "Packet", description = "Invalid rotations")
public final class ScaffoldF extends Check {
    public ScaffoldF(PlayerData data) {
        super(data);
    }

    private long lastPlace, placeDelay, lastSneak, sneakDelay;
    private boolean bridging, sneaked;

    @Override
    public void handle(Packet p) {
        if (!p.isEntityAction()) return;
        WrapperPlayClientEntityAction.Action a = p.entityAction().getAction();
        if (a == WrapperPlayClientEntityAction.Action.START_SNEAKING || a == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
            sneakDelay = System.currentTimeMillis() - lastSneak;
            sneaked = true;
            lastSneak = System.currentTimeMillis();
        }
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent)) return;
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        if (e.isCancelled() || Blocks.SCAFFOLDING.is(e.getBlock().getType()) || !e.getBlock().getType().isSolid()) return;
        bridging = data.action.bridging;
        placeDelay = System.currentTimeMillis() - lastPlace;
        lastPlace = System.currentTimeMillis();
        double dxz = data.position.deltaXZ;
        // limit from walk speed: always 0.27 in the original (walk speed is constant)
        float limit = 0.27f;
        boolean exempt = exempt(Exempt.LIQUID, Exempt.FLIGHT, Exempt.CREATIVE, Exempt.VEHICLE);
        if (sneaked && bridging && dxz > limit && placeDelay < 400L && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + placeDelay + " delta=" + dxz + " sd=" + sneakDelay);
        } else {
            decreaseBuffer();
        }
        sneaked = false;
    }
}
