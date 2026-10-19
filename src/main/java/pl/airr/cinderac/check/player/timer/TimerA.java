package pl.airr.cinderac.check.player.timer;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Samples;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Timer", type = 'A', complexType = "Average", description = "Increased game speed.")
public final class TimerA extends Check {
    public TimerA(PlayerData data) {
        super(data);
    }

    private final Samples<Long> delays = new Samples<>(50);
    private long lastMove;

    @Override
    public void handle(Packet p) {
        if ((p.isFlying() || p.isVehicleMove()) && !isAtOrigin()) {
            long delay = System.currentTimeMillis() - lastMove;
            boolean exempt = exempt(Exempt.JOINED, Exempt.TPS, Exempt.VEHICLE);
            if (delay > 4L && !exempt) delays.add(delay);
            if (delays.isFull()) {
                double speed = 50.0 / VMath.mean(delays);
                double max = cfg.getDouble("max-speed", 1.03);
                if (exempt(Exempt.WEB)) max += 0.05;
                if (speed >= max) {
                    if (increaseBuffer() > maxBuffer()) fail("speed=" + speed * 100.0 + "% delay=" + delay);
                } else {
                    decreaseBuffer();
                }
            }
            lastMove = System.currentTimeMillis();
        } else if (p.isServerTeleport()) {
            delays.add(150L);
        } else if (data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_17) && (p.isBlockPlace() || p.isUseItem())) {
            // 1.17+ clients skip a movement packet when using an item - counted as a tick
            delays.add(50L);
        }
    }
}
