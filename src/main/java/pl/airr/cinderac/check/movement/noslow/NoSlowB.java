package pl.airr.cinderac.check.movement.noslow;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "No Slow", type = 'B', complexType = "Soul Sand", description = "Moving too quickly on Soul Sand.")
public final class NoSlowB extends Check {
    public NoSlowB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        double dxz = data.position.deltaXZ;
        double max = 0.2f + data.action.speedAmplifier * 0.0625f;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.CREATIVE, Exempt.VELOCITY, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHORUS_FRUIT,
                Exempt.SOUL_SPEED, Exempt.ELYTRA, Exempt.RIPTIDE, Exempt.HIGH_SPEED, Exempt.DEPTH_STRIDER);
        boolean invalid = data.position.soulSandAtFeet && dxz > max && data.position.mathematicallyOnGround;
        // walk speed in the original is a constant 0.2 (> 0.11), so this always exempts the player - the check never flags
        boolean fast = data.position.walkSpeed > 0.11f || data.action.movementSpeedAttribute > 0.11f;
        if (invalid && !exempt && !fast) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz + " max=" + max);
        } else {
            decreaseBuffer();
        }
    }
}
