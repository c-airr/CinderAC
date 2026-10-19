package pl.airr.cinderac.check.movement.entityflight;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Entity Flight", type = 'A', complexType = "Ascension", description = "Ascending while riding an entity.")
public final class EntityFlightA extends Check {
    public EntityFlightA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9) || data.player == null) return;
        PositionTracker pos = data.position;
        if (pos.vehicleY < 0.0) return;
        Entity vehicle = data.player.getVehicle();
        String label = mountLabel(vehicle);
        if (label == null) return;
        LivingEntity mount = (LivingEntity) vehicle;
        if (mount.isLeashed()) return;
        if (ServerVersion.atLeast(13, 1) && (mount.hasPotionEffect(PotionEffectType.LEVITATION)
                || mount.hasPotionEffect(PotionEffectType.SLOW_FALLING))) return;
        boolean blocked = pos.sinceVehicleLiquidTicks < 100 || pos.sinceVehicleSlimeTicks < 100 || pos.sinceVehicleBubbleTicks < 100;
        int air = pos.vehicleAirTicks;
        double dy = pos.vehicleDeltaY;
        boolean invalid = air > 30 && dy > 0.0 && !blocked && pos.vehicleTicks > 5;
        if (invalid) {
            if (increaseBuffer() > maxBuffer()) {
                fail("[" + label + "] deltaY=" + dy + " ticks=" + air);
                if (cfg.getBoolean("kick-out", false)) {
                    org.bukkit.entity.Player pl = data.player;
                    CinderAC.get().tasks().runForPlayer(pl, pl::leaveVehicle);
                }
            }
        } else {
            decreaseBuffer();
        }
    }

    /** Mount label as in the original; null = other vehicle (not checked). Uses the type name because classes differ between versions. */
    static String mountLabel(Entity vehicle) {
        if (!(vehicle instanceof LivingEntity)) return null;
        switch (vehicle.getType().name()) {
            case "HORSE":
                return "Horse";
            case "PIG":
                return "Pig";
            case "DONKEY":
                return "Donkey";
            case "ZOMBIE_HORSE":
                return "Zombie Horse";
            case "SKELETON_HORSE":
                return "Skeleton Horse";
            case "CAMEL":
                return ServerVersion.atLeast(20) ? "Camel" : null;
            default:
                return null;
        }
    }
}
