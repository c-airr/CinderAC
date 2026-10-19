package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Elytra", type = 'M', complexType = "Ascending", description = "Invalid ascension pattern.")
public final class ElytraM extends Check {
    public ElytraM(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || !data.action.glidingBukkit) return;
        double dy = data.position.deltaY;
        if (Math.abs(dy) <= 0.01) return;
        double rest = 0.01 % (dy % 0.01);
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.SLIME, Exempt.SLOW_FALLING, Exempt.LEVITATION, Exempt.LIQUID, Exempt.BUBBLE_COLUMN,
                Exempt.RIPTIDE, Exempt.CLIMBABLE);
        if (rest < 1.0E-8 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " m=" + rest);
        } else {
            decreaseBuffer();
        }
    }
}
