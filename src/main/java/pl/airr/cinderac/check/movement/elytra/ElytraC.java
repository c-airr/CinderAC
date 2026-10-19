package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'C', complexType = "Acceleration", description = "Invalid acceleration.")
public final class ElytraC extends Check {
    public ElytraC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        if (!ServerVersion.atLeast(9) || !data.action.glidingBukkit) return;
        int ticks = data.position.glidingTicks, nearBlock = data.position.allAirTicks;
        double dy = data.position.deltaY, dxz = data.position.deltaXZ;
        boolean gliding = data.player != null && data.player.isGliding();
        boolean exempt = exempt(Exempt.FIREWORK, Exempt.EXPLOSION, Exempt.RIPTIDE, Exempt.CHUNK, Exempt.DRAGON_DAMAGE, Exempt.FLIGHT, Exempt.SLIME);
        double accel = dxz - data.position.lastDeltaXZ;
        if (ticks > 3 && nearBlock > 3 && gliding && !exempt && accel > 0.035 && dy > -0.6) {
            if (increaseBuffer() > maxBuffer()) fail("accel=" + accel + " deltaXZ=" + dxz + " deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
