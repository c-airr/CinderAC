package pl.airr.cinderac.check.movement.boatfly;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Boat Fly", type = 'C', complexType = "Hover", description = "Hovering in a boat.")
public final class BoatFlyC extends Check {
    public BoatFlyC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9) || data.player == null) return;
        Entity vehicle = data.player.getVehicle();
        if (!(vehicle instanceof Boat) || data.position.vehicleTicks <= 5) return;
        PositionTracker pos = data.position;
        if (CinderAC.get().fishedBoatTick(vehicle.getEntityId()) != null) return;
        double dy = pos.vehicleDeltaY, accel = Math.abs(dy - pos.lastVehicleDeltaY);
        boolean hover = dy > -0.05 && accel < 0.025 && pos.vehicleAirTicks > 5;
        if (pos.vehicleNearLiving) return;
        if (ServerVersion.atLeast(13, 1) && !vehicle.hasGravity()) return;
        boolean blocked = pos.sinceVehicleIceTicks < 50 || pos.sinceVehicleLiquidTicks < 50 || pos.sinceVehicleSlimeTicks < 50
                || pos.sinceVehicleBubbleTicks < 50 || pos.sinceVehiclePistonTicks < 50;
        if (hover && !blocked && pos.vehicleNearBoats == 1) {
            if (increaseBuffer() > maxBuffer()) fail("accel=" + accel + " deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
