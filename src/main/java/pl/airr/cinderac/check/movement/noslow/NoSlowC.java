package pl.airr.cinderac.check.movement.noslow;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "No Slow", type = 'C', complexType = "Web", description = "Moving too quickly in a web.")
public final class NoSlowC extends Check {
    public NoSlowC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        double dxz = data.position.deltaXZ;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.CREATIVE, Exempt.VELOCITY, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHORUS_FRUIT,
                Exempt.RIPTIDE, Exempt.ELYTRA, Exempt.GLIDING, Exempt.DEPTH_STRIDER, Exempt.SERVER_POSITION_FAST, Exempt.SWIMMING, Exempt.TRAPDOOR);
        if (data.position.webAtBody && dxz > 0.09 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaXZ=" + dxz + " max=0.09");
        } else {
            decreaseBuffer();
        }
    }
}
