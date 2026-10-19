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

@CheckInfo(name = "Boat Fly", type = 'B', complexType = "Vertical", description = "Moving too quickly horizontally.")
public final class BoatFlyB extends Check {
    public BoatFlyB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9) || data.player == null) return;
        Entity vehicle = data.player.getVehicle();
        if (!(vehicle instanceof Boat) || data.position.vehicleTicks <= 5) return;
        PositionTracker pos = data.position;
        double dxz = pos.vehicleDeltaXZ, dy = pos.vehicleDeltaY;
        if (CinderAC.get().fishedBoatTick(vehicle.getEntityId()) != null) return;
        if (pos.vehicleNearLiving) return;
        if (ServerVersion.atLeast(13, 1) && !vehicle.hasGravity()) return;
        double max = 0.25;
        if (exempt(Exempt.WATERLOGGED)) max += 0.2;
        boolean blocked = pos.sinceVehicleLiquidTicks < 100 || pos.sinceVehicleSlimeTicks < 100 || pos.sinceVehicleBubbleTicks < 100
                || pos.sinceVehicleIceTicks < 100 || pos.sinceVehiclePistonTicks < 100;
        if (dxz > max && dy > -0.05 && !blocked && pos.vehicleNearBoats == 1) {
            if (increaseBuffer() > maxBuffer()) {
                fail("deltaXZ=" + dxz + " deltaY=" + dy);
                kickOut();
            }
        } else {
            decreaseBuffer();
        }
    }

    private void kickOut() {
        if (cfg.getBoolean("kick-out", false) && data.player != null) {
            org.bukkit.entity.Player pl = data.player;
            CinderAC.get().tasks().runForPlayer(pl, pl::leaveVehicle);
        }
    }
}
