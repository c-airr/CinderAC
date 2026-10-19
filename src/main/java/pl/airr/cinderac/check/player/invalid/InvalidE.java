package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'E', complexType = "X/Z", description = "Too large X/Z-Axis movement.")
public final class InvalidE extends Check {
    public InvalidE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        double dxz = data.position.deltaXZ;
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.GLIDING, Exempt.VELOCITY, Exempt.JOINED, Exempt.FLIGHT,
                Exempt.CREATIVE, Exempt.VEHICLE, Exempt.WORLD_CHANGE, Exempt.DOLPHINS_GRACE, Exempt.SOUL_SPEED, Exempt.CHORUS_FRUIT,
                Exempt.SLEEPING, Exempt.LEVITATION, Exempt.ENDER_PEARL, Exempt.ENTITY_CRAM_FIX, Exempt.HIGH_SPEED, Exempt.SHULKER_BOX,
                Exempt.SHULKER, Exempt.CHUNK);
        boolean invalid = dxz > 3.5 && dxz < 100.0 && data.action.movementSpeedAttribute < 0.25;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz);
        } else {
            decreaseBuffer();
        }
    }
}
