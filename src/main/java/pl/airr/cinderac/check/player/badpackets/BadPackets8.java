package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = '8', complexType = "Nuker", description = "Invalid Block Dig packets.")
public final class BadPackets8 extends Check {
    public BadPackets8(PlayerData data) {
        super(data);
    }


    private int digs;
    private long lastDig = -1L;

    private static boolean ignored(DiggingAction a) {
        return a == DiggingAction.DROP_ITEM || a == DiggingAction.DROP_ITEM_STACK || a == DiggingAction.RELEASE_USE_ITEM
                || a == DiggingAction.SWAP_ITEM_WITH_OFFHAND || a == DiggingAction.CANCELLED_DIGGING;
    }

    @Override
    public void handle(Packet p) {
        if (data.hasTickEnd()) {
            if (p.isDigging()) {
                if (ignored(p.digging().getAction())) return;
                if (++digs > 3) {
                    if (increaseBuffer() > maxBuffer()) fail("digs=" + digs);
                } else {
                    decreaseBuffer();
                }
            } else if (p.isClientTickEnd()) {
                digs = 0;
            }
            return;
        }
        if (!p.isDigging() || ignored(p.digging().getAction())) return;
        long now = System.currentTimeMillis();
        long delay = now - lastDig;
        boolean lagging = now - data.keepAlive.lastFastFlyingMillis < 1000L;
        if (delay < 10L && !lagging) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + delay);
        } else {
            decreaseBuffer();
        }
        lastDig = System.currentTimeMillis();
    }
}
