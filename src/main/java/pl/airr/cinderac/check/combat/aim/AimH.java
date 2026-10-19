package pl.airr.cinderac.check.combat.aim;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'H', complexType = "Negative", description = "Invalid sensitivity.")
public final class AimH extends Check {
    public AimH(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3) return;
        double sens = data.rotation.sensitivityPercent;
        double dp = data.rotation.deltaPitch;
        PlayerData t = target();
        if (t == null) return;
        boolean cinematic = exempt(Exempt.CINEMATIC);
        boolean low = data.rotation.lowSensitivity();
        if (sens < -1.0 && dp < 20.0 && dp > 0.25 && dp > 1.25 && t.position.deltaXZ > 0.01 && !cinematic && !low) {
            if (increaseBuffer() > maxBuffer()) fail("final=" + sens + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }

    private PlayerData target() {
        Entity e = data.combat.lastAttacked;
        return e instanceof Player ? CinderAC.get().players().get((Player) e) : null;
    }
}
