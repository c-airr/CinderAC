package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Scaffold", type = 'D', complexType = "Rotations", description = "Invalid rotations.")
public final class ScaffoldD extends Check {
    public ScaffoldD(PlayerData data) {
        super(data);
    }

    private long lastPlace, placeDelay;
    private boolean bridging;
    private int packets;

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof BlockPlaceEvent) || ((BlockPlaceEvent) event).isCancelled()) return;
        bridging = data.action.bridging;
        placeDelay = System.currentTimeMillis() - lastPlace;
        packets = 0;
        lastPlace = System.currentTimeMillis();
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || ++packets >= 6 || !bridging || placeDelay >= 400L) return;
        float last = data.rotation.lastDeltaPitch, dp = data.rotation.deltaPitch;
        long gcd = VMath.gcd((long) (VMath.EXPANDER * (double) dp), (long) (VMath.EXPANDER * (double) last));
        boolean exempt = exempt(Exempt.CINEMATIC) || data.action.swiftSneak;
        if (gcd < 131072L && gcd > 0L && (double) dp > 0.5 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("rotation=" + gcd / 100L + " dp=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
