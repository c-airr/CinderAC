package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Speed", type = 'A', complexType = "Friction", description = "Invalid friction.")
public final class SpeedA extends Check {
    public SpeedA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        if (data.action.sinceEnderCrystalDamageTicks < 3) return;
        double dxz = data.position.deltaXZ;
        double diff = dxz - (data.position.lastDeltaXZ * 0.91f + 0.026);
        int air = data.position.airTicks;
        boolean piston = data.position.sincePistonAroundTicks < 15;
        boolean velocity = data.velocity.sinceVelocityTicks < 3;
        boolean exempt = exempt(Exempt.ILLEGAL_BLOCK, Exempt.FULLY_STUCK, Exempt.DOLPHINS_GRACE, Exempt.WEB, Exempt.SLEEPING, Exempt.GLIDING,
                Exempt.ELYTRA, Exempt.DEPTH_STRIDER, Exempt.VOID, Exempt.WORLD_CHANGE, Exempt.CHUNK, Exempt.JOINED, Exempt.ENTITY_COLLISION,
                Exempt.ANVIL, Exempt.TRAPDOOR, Exempt.SPECTATOR, Exempt.PARTIALLY_STUCK, Exempt.FENCE_GATE, Exempt.FROZEN, Exempt.CHORUS_FRUIT,
                Exempt.BLOCK_BREAK, Exempt.CANCELLED_MOVE, Exempt.FLIGHT, Exempt.CREATIVE, Exempt.TELEPORT, Exempt.LIQUID, Exempt.RIPTIDE,
                Exempt.ENDER_PEARL, Exempt.VEHICLE);
        boolean explosion = data.velocity.sinceExplosionTicks < 5;
        // buffer unchanged when the preconditions are not met
        if (air <= 2 || velocity || exempt || piston || dxz <= 0.25 || explosion) return;
        if (diff > 1.0E-5) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff + " ticks=" + air + " deltaXZ=" + dxz);
        } else {
            decreaseBuffer();
        }
    }
}
