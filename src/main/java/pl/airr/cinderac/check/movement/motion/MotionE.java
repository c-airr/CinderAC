package pl.airr.cinderac.check.movement.motion;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Motion", type = 'E', complexType = "Modulo", description = "Impossible movement values.")
public final class MotionE extends Check {
    public MotionE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || data.player == null) return;
        if (data.action.hasSlowness || data.player.getWalkSpeed() > 0.25f || data.action.speedAmplifier > 3) return;
        double dxz = data.position.deltaXZ;
        double rest = 0.1 % (dxz % 0.1);
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.GLIDING, Exempt.STAIRS, Exempt.VELOCITY, Exempt.JOINED, Exempt.FLIGHT,
                Exempt.CREATIVE, Exempt.VEHICLE, Exempt.WORLD_CHANGE, Exempt.DOLPHINS_GRACE, Exempt.SOUL_SPEED, Exempt.SLIME, Exempt.HONEY,
                Exempt.DEATH, Exempt.SLEEPING, Exempt.LEVITATION, Exempt.LIQUID, Exempt.ANVIL, Exempt.SERVER_POSITION, Exempt.PISTON, Exempt.WEB);
        if (dxz > 0.11 && rest < 1.0E-8 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz + " m=" + rest);
        } else {
            decreaseBuffer();
        }
    }
}
