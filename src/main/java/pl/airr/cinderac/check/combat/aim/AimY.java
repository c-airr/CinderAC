package pl.airr.cinderac.check.combat.aim;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Aim", type = 'Y', complexType = "Rotation", description = "Generic rotation analysis heuristic.")
public final class AimY extends Check {
    public AimY(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isRotation() || hitTicks() >= 3 || isServerPositionTick()) return;
        float diff = Math.abs(data.rotation.yawRounded - data.rotation.lastYawRounded);
        double dist = data.combat.targetDistance;
        if (exempt(Exempt.TELEPORT, Exempt.DEATH, Exempt.WORLD_CHANGE) || data.action.sinceServerPositionTicks < 20) return;
        if (dist > 0.6 && diff > 20.0f && dist < 10.0) fail("diff=" + diff + " dist=" + dist);
    }
}
