package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Samples;

import java.util.List;

/**
 * Sample of delays between clicks (ms). A delay > 5 s clears the sample.
 * rolling = sliding window (evaluated on every click once full), otherwise evaluated per full sample and then cleared.
 */
abstract class ClickCheck extends Check {
    private final boolean rolling;
    private final int size;
    final List<Long> samples;

    ClickCheck(PlayerData data, int size, boolean rolling) {
        super(data);
        this.size = size;
        this.rolling = rolling;
        this.samples = new Samples<>(rolling ? size : Integer.MAX_VALUE);
    }

    abstract void evaluate(List<Long> s);

    @Override
    public void handle(Packet p) {
        if (!p.isAnimation() || exempt(Exempt.AUTOCLICKER)) return;
        long delay = data.clicks.delay;
        if (delay > 5000L) {
            samples.clear();
            return;
        }
        samples.add(delay);
        if (rolling ? samples.size() >= size : samples.size() == size) {
            evaluate(samples);
            if (!rolling) samples.clear();
        }
    }
}
