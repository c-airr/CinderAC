package pl.airr.cinderac.check.movement.entityspeed;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Attributes;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Entity Speed", type = 'A', complexType = "Limit", description = "Riding an entity too quickly.")
public final class EntitySpeedA extends Check {
    public EntitySpeedA(PlayerData data) {
        super(data);
    }

    /** Ticks since the mount last had a Speed effect (starts high). */
    private int sinceMountSpeed = 10000;

    @Override
    public void handle(Packet p) {
        if (!p.isVehicleMove() || !ServerVersion.atLeast(9) || data.player == null) return;
        PositionTracker pos = data.position;
        boolean ice = pos.sinceVehicleIceTicks < 100;
        if (pos.sinceVehiclePistonTicks < 40) return;
        int ticks = pos.vehicleTicks;
        double speed = pos.vehicleDeltaXZ;
        boolean onGrid = pos.vehicleY % 0.015625 == 0.0;
        Entity vehicle = data.player.getVehicle();
        if (vehicle instanceof LivingEntity) {
            sinceMountSpeed = speedLevel((LivingEntity) vehicle) > 0 ? 0 : sinceMountSpeed + 1;
        }
        if (vehicle instanceof Boat) {
            double max = 0.65;
            if (pos.sinceVehicleLiquidTicks < 100) max += 0.15;
            double diff = speed - max;
            boolean blocked = ice || pos.sinceVehicleBubbleTicks < 100 || pos.sinceVehicleSlimeTicks < 100;
            verdict(diff > minDiff() && !blocked && ticks > 5, "[Boat] deltaXZ=" + speed + " max=" + max + " diff=" + diff);
            return;
        }
        if (!(vehicle instanceof LivingEntity)) return;
        LivingEntity mount = (LivingEntity) vehicle;
        String type = vehicle.getType().name();
        // TraderLlama extends Llama, so the original always takes the llama branch
        if (type.equals("TRADER_LLAMA")) type = "LLAMA";
        String label;
        double potionMul, ampMul, base = 0.1;
        boolean needGrid = false;
        switch (type) {
            case "HORSE":
                if (mount.hasMetadata("PROCOSMETICS_ENTITY")) return;
                label = "Horse"; potionMul = 0.5; ampMul = 0.085;
                break;
            case "MULE":
            case "DONKEY":
            case "ZOMBIE_HORSE":
                if (!ServerVersion.atLeast(12)) return;
                label = type.equals("MULE") ? "Mule" : type.equals("DONKEY") ? "Donkey" : "Zombie Horse";
                potionMul = 0.5; ampMul = 0.085;
                break;
            case "SKELETON_HORSE":
                if (!ServerVersion.atLeast(12)) return;
                label = "Skeleton Horse"; potionMul = 0.15; ampMul = 0.065;
                break;
            case "PIG":
                label = "Pig"; potionMul = 0.5; ampMul = 0.0; needGrid = true;
                break;
            case "STRIDER":
                if (!ServerVersion.atLeast(16)) return;
                label = "Strider"; potionMul = 0.15; ampMul = 0.065;
                break;
            case "LLAMA":
                if (!ServerVersion.atLeast(11)) return;
                label = "Llama"; potionMul = 0.45; ampMul = 0.085;
                break;
            case "CAMEL":
                if (!ServerVersion.atLeast(11)) return;
                label = "Camel"; potionMul = 0.45; ampMul = 0.085; base = 0.45;
                break;
            default:
                return;
        }
        if (mount.isLeashed()) return;
        double max = Attributes.movementSpeed(mount) * 2.25 + base;
        if (mount.hasPotionEffect(PotionEffectType.SPEED)) max += speedLevel(mount) * potionMul;
        // pigs get no bonus for the player effect or for being off the 1/64 grid
        if (!needGrid) {
            if (data.action.hasSpeed) max += data.action.speedAmplifier * ampMul;
            if (!onGrid) max += 0.175f;
        }
        if (label.equals("Skeleton Horse") && pos.sinceVehicleLiquidTicks < 20) max += 0.3;
        if (sinceMountSpeed < 20 && sinceMountSpeed > 0) max += 0.5;
        if (label.equals("Camel") && ((Camel) mount).isDashing()) max += 0.25;
        double diff = speed - max;
        verdict(diff > minDiff() && !ice && ticks > 10 && (!needGrid || onGrid),
                "[" + label + "] speed=" + speed + " maxSpeed=" + max + " diff=" + diff);
    }

    private double minDiff() {
        return cfg.getDouble("min-difference", 0.0001);
    }

    private void verdict(boolean invalid, String info) {
        if (invalid) {
            if (increaseBuffer() > maxBuffer()) {
                fail(info);
                if (cfg.getBoolean("kick-out", false)) {
                    org.bukkit.entity.Player pl = data.player;
                    CinderAC.get().tasks().runForPlayer(pl, pl::leaveVehicle);
                }
            }
        } else {
            decreaseBuffer();
        }
    }

    /** Speed effect level on the entity (amplifier + 1), 0 when absent. */
    private static int speedLevel(LivingEntity e) {
        for (PotionEffect effect : e.getActivePotionEffects()) {
            if (effect.getType().toString().toLowerCase().contains("speed")) return effect.getAmplifier() + 1;
        }
        return 0;
    }
}
