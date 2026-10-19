package pl.airr.cinderac.check.combat.aim;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'F', complexType = "Straight", description = "Invalid yaw change.")
public final class AimF extends Check {
    public AimF(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || !recentlyAttacked(3)) return;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch, pitch = data.rotation.pitch;
        double dist = data.combat.targetDistance;
        PlayerData t = target();
        if (t == null) return;
        boolean targetVehicle = t.exempt(Exempt.VEHICLE);
        if (dp == 0.0f && dy > 1.825f && Math.abs(pitch) < 60.0f && dist > 1.0 && t.position.deltaXZ > 0.03 && !targetVehicle) {
            if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
        } else {
            decreaseBuffer();
        }
    }

    private PlayerData target() {
        Entity e = data.combat.lastAttacked;
        return e instanceof Player ? CinderAC.get().players().get((Player) e) : null;
    }
}
