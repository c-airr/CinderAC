package pl.airr.cinderac.check.combat.aim;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'O', complexType = "Small Yaw", description = "Too small pitch change.")
public final class AimO extends Check {
    public AimO(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || !recentlyAttacked(2)) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        boolean invalid = (double) dp < 0.05 && dp > 0.0f && dy == 0.0f;
        PlayerData t = target();
        if (t == null) return;
        if (t.position.deltaXZ < 0.03) return;
        if (invalid) {
            if (increaseBuffer() > maxBuffer()) fail("deltaPitch=" + dp + " deltaYaw=" + dy);
        } else {
            decreaseBuffer();
        }
    }

    private PlayerData target() {
        Entity e = data.combat.lastAttacked;
        return e instanceof Player ? CinderAC.get().players().get((Player) e) : null;
    }
}
