package pl.airr.cinderac.data;

import pl.airr.cinderac.util.Samples;
import pl.airr.cinderac.util.VMath;

/** Delays between arm swings (ArmAnimation) -> CPS. */
public final class ClickTracker {
    private final PlayerData data;

    public long lastSwing = -1L, delay;
    public long lastAnimalInteract = System.currentTimeMillis(), lastItemFrameInteract = System.currentTimeMillis();
    public long lastNoteBlockInteract = System.currentTimeMillis();
    public final Samples<Long> delays = new Samples<>(20);
    public double cps;

    public ClickTracker(PlayerData data) {
        this.data = data;
    }

    public void handleAnimation() {
        if (data.exempt(Exempt.AUTOCLICKER_NON_DIG)) return;
        long now = System.currentTimeMillis();
        if (lastSwing > 0L) {
            delay = now - lastSwing;
            delays.add(delay);
            if (delays.isFull()) cps = VMath.cps(delays);
        }
        lastSwing = now;
    }
}
