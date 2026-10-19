package pl.airr.cinderac.check.movement.entityflight;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;
import static pl.airr.cinderac.check.movement.entityflight.EntityFlightA.mountLabel;

@CheckInfo(name = "Entity Flight", type = 'B', complexType = "Hover", description = "Hovering while riding an entity.")
public final class EntityFlightB extends Check {
    public EntityFlightB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9) || data.player == null) return;
        PositionTracker pos = data.position;
        if (pos.vehicleY < 0.0 || data.player.isDead()) return;
        Entity vehicle = data.player.getVehicle();
        String label = mountLabel(vehicle);
        if (label == null) return;
        LivingEntity mount = (LivingEntity) vehicle;
        if (mount.isLeashed()) return;
        if (ServerVersion.atLeast(13, 1) && (mount.hasPotionEffect(PotionEffectType.LEVITATION)
                || mount.hasPotionEffect(PotionEffectType.SLOW_FALLING))) return;
        boolean blocked = pos.sinceVehicleLiquidTicks < 100 || pos.sinceVehicleSlimeTicks < 100 || pos.sinceVehicleBubbleTicks < 100;
        int air = pos.vehicleAirTicks;
        double dy = pos.vehicleDeltaY, accel = Math.abs(dy - pos.lastVehicleDeltaY);
        boolean hover = air > 30 && Math.abs(dy) < 5.0 && accel < 0.05 && !blocked && pos.vehicleTicks > 5 && dy > -1.15;
        if (hover) {
            if (increaseBuffer() > maxBuffer()) fail("[" + label + "] deltaY=" + dy + " accel=" + accel + " ticks=" + pos.vehicleTicks);
        } else {
            decreaseBuffer();
        }
    }
}
