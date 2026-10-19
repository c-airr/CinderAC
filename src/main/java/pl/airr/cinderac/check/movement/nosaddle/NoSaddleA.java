package pl.airr.cinderac.check.movement.nosaddle;

import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Attributes;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "No Saddle", type = 'A', complexType = "Spoof", description = "Controlling an entity without a saddle.")
public final class NoSaddleA extends Check {
    public NoSaddleA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9)) return;
        double dxz = data.position.vehicleDeltaXZ;
        if (data.player == null || !(data.player.getVehicle() instanceof Horse)) return;
        Horse horse = (Horse) data.player.getVehicle();
        if (horse.isLeashed()) return;
        double speed = Attributes.movementSpeed(horse) * 2.5 + 0.1;
        ItemStack saddle = horse.getInventory().getSaddle();
        double diff = speed - dxz;
        boolean invalid = saddle == null && diff < 0.3 && dxz > 0.15;
        if (invalid && data.position.vehicleTicks > 5) {
            if (increaseBuffer() > maxBuffer()) {
                fail("diff=" + diff + " deltaXZ=" + dxz);
                if (cfg.getBoolean("kick-out", false)) {
                    org.bukkit.entity.Player pl = data.player;
                    CinderAC.get().tasks().runForPlayer(pl, pl::leaveVehicle);
                }
            }
        } else {
            decreaseBuffer();
        }
    }
}
