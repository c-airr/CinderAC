package pl.airr.cinderac.check.combat.autoclicker;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

import java.util.LinkedList;
import java.util.List;

@CheckInfo(name = "Auto Clicker", type = 'A', complexType = "Limit", description = "Left clicking too quickly.")
public final class AutoClickerA extends Check {
    public AutoClickerA(PlayerData data) {
        super(data);
    }

    private final List<Long> delays = new LinkedList<>();
    private long lastSwing;

    @Override
    public void handle(Packet p) {
        if (!p.isAnimation() || exempt(Exempt.AUTOCLICKER_NON_DIG)) return;
        long now = System.currentTimeMillis();
        delays.add(now - lastSwing);
        if (delays.size() == 20) {
            double cps = VMath.cps(delays);
            if (cps > cfg.getInt("max-cps", 25) && cps > 5.0) fail("cps=" + cps);
            delays.clear();
        }
        lastSwing = now;
    }
}
