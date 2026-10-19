package pl.airr.cinderac.check.movement.sprint;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Sprint", type = 'A', complexType = "Angle", description = "Invalid sprint direction.")
public final class SprintA extends Check {
    public SprintA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        float angle = data.rotation.moveAngle();
        int ground = data.position.groundTicks, sprint = data.action.sprintingTicks;
        double dxz = data.position.deltaXZ;
        boolean invalid = sprint > 10 && angle > 1.5 && ground > 6 && dxz > 0.25;
        boolean exempt = exempt(Exempt.RIPTIDE, Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.JOINED, Exempt.SERVER_POSITION, Exempt.SWIMMING,
                Exempt.LIQUID, Exempt.ICE);
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("angle=" + angle + " ticks=" + sprint);
        } else {
            decreaseBuffer();
        }
    }
}
