package pl.airr.cinderac.check.player.scaffold;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Scaffold", type = 'H', complexType = "Rotations [3]", description = "Invalid rotations.")
public final class ScaffoldH extends Check {
    public ScaffoldH(PlayerData data) {
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
        if (!p.isRotation() || ++packets >= 5 || !bridging || placeDelay >= 400L) return;
        double sens = data.rotation.sensitivityPercent;
        float dp = data.rotation.deltaPitch;
        boolean invalid = sens < -2.0 || sens > 200.0;
        boolean exempt = exempt(Exempt.CINEMATIC) || data.action.swiftSneak;
        if (invalid && !exempt && data.position.deltaXZ > 0.25) {
            if (increaseBuffer() > maxBuffer()) fail("final=" + sens + " dp=" + dp);
        } else {
            decreaseBuffer();
        }
    }
}
