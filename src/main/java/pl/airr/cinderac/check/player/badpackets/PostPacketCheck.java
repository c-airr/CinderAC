package pl.airr.cinderac.check.player.badpackets;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.PlayerData;

/**
 * "Post" packet: arrived < 5 ms after a movement packet (i.e. after it within the same client tick).
 * Flags when the next movement packet arrives 40..100 ms later - a normal tick, so it is not lag.
 */
abstract class PostPacketCheck extends Check {
    private long lastFlying;
    private boolean sent;

    PostPacketCheck(PlayerData data) {
        super(data);
    }

    abstract boolean isTarget(Packet p);

    /** true = skip the movement packet entirely (no state change). */
    boolean skip() {
        return false;
    }

    /** true = do not flag (state is still reset). */
    boolean exempt() {
        return false;
    }

    void flag(long delay) {
        fail("delay=" + delay);
    }

    @Override
    public void handle(Packet p) {
        if (p.isFlying()) {
            long delay = System.currentTimeMillis() - lastFlying;
            if (skip()) return;
            if (sent && !exempt()) {
                if (delay > 40L && delay < 100L) {
                    if (increaseBuffer() > maxBuffer()) flag(delay);
                } else {
                    decreaseBuffer();
                }
                sent = false;
            }
            lastFlying = System.currentTimeMillis();
        } else if (isTarget(p) && System.currentTimeMillis() - lastFlying < 5L) {
            sent = true;
        }
    }
}
