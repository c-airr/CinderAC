package pl.airr.cinderac.check.movement.strafe;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Strafe", type = 'A', complexType = "Air", description = "Moving incorrectly in the air.")
public final class StrafeA extends Check {
    public StrafeA(PlayerData data) {
        super(data);
    }

    private boolean lastSprinting;

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        boolean sprinting = data.action.sprinting;
        int notSprinting = data.action.ticksNotSprinting, air = data.position.airTicks;
        double predictedX = data.position.lastDeltaX * 0.91f, predictedZ = data.position.lastDeltaZ * 0.91f;
        double diff = VMath.hypot(data.position.deltaX - predictedX, data.position.deltaZ - predictedZ);
        diff /= lastSprinting ? 1.3 : 1.0;
        diff -= !sprinting && notSprinting >= 2 ? 0.02 : 0.026;
        double dxz = data.position.deltaXZ;
        boolean velocity = data.velocity.sinceVelocityTicks < 3;
        boolean piston = data.position.sincePistonTicks < 30;
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.PARTIALLY_STUCK, Exempt.FIREWORK, Exempt.DEATH, Exempt.JOINED, Exempt.WORLD_CHANGE,
                Exempt.FLIGHT, Exempt.FULLY_STUCK, Exempt.GLIDING, Exempt.CREATIVE, Exempt.SPECTATOR, Exempt.SOUL_SAND, Exempt.ELYTRA,
                Exempt.VEHICLE, Exempt.ENTITY_COLLISION, Exempt.DEPTH_STRIDER, Exempt.FROZEN, Exempt.LIQUID, Exempt.DOLPHINS_GRACE,
                Exempt.RIPTIDE, Exempt.SWIMMING, Exempt.CLIMBABLE, Exempt.ENDER_PEARL, Exempt.FIREBALL, Exempt.CHORUS_FRUIT, Exempt.BLOCK_BREAK,
                Exempt.CANCELLED_MOVE);
        if (data.position.sinceClimbableTicks < 100) return;
        boolean explosion = data.velocity.sinceExplosionTicks < 10;
        boolean counter = data.action.counterC < 30;
        double max = 0.0075;
        if (data.position.sinceBlocksAroundTicks < 5) max += 0.04;
        boolean invalid = diff > max && dxz > 0.25 && air > 3;
        if (invalid && !exempt && !piston && !velocity && !explosion && !counter) {
            if (increaseBuffer() > maxBuffer()) fail("difference=" + diff + " deltaXZ=" + dxz);
        } else {
            decreaseBuffer();
        }
        lastSprinting = sprinting;
    }
}
