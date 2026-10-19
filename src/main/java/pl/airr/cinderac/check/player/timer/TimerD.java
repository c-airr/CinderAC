package pl.airr.cinderac.check.player.timer;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Timer", type = 'D', complexType = "Balance", description = "Increased game speed.")
public final class TimerD extends Check {
    public TimerD(PlayerData data) {
        super(data);
    }

    private long lastMove, balance;
    private int count;

    @Override
    public void handle(Packet p) {
        if ((p.isFlying() || p.isVehicleMove()) && !isAtOrigin()) {
            long delay = System.currentTimeMillis() - lastMove;
            boolean exempt = exempt(Exempt.JOINED, Exempt.TPS, Exempt.VEHICLE);
            if (lastMove != 0L && !exempt) {
                balance += 50L;
                balance -= delay;
                if (balance > cfg.getInt("max-balance", 200)) {
                    if (increaseBuffer() > maxBuffer()) fail("balance=" + balance + " delay=" + delay);
                    else decreaseBuffer();
                    balance = -200L;
                }
                int every = data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 400 : 3000;
                if (count % every == 0) {
                    balance = -200L;
                    count = 0;
                }
            }
            ++count;
            lastMove = System.currentTimeMillis();
        } else if (p.isServerTeleport()) {
            balance -= ServerVersion.atLeast(9) ? 150L : 50L;
        }
    }
}
