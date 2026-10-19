package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Elytra", type = 'L', complexType = "Ascending", description = "Moving up wrongly.")
public final class ElytraL extends Check {
    public ElytraL(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || !data.action.glidingBukkit) return;
        double dy = data.position.deltaY;
        float pitch = data.rotation.pitch;
        boolean exempt = exempt(Exempt.DRAGON_DAMAGE, Exempt.EXPLOSION, Exempt.FIREWORK, Exempt.TELEPORT, Exempt.FISHING_ROD, Exempt.RIPTIDE,
                Exempt.BUBBLE_COLUMN, Exempt.LIQUID, Exempt.SLIME, Exempt.PISTON, Exempt.WORLD_CHANGE, Exempt.SWIMMING, Exempt.BED,
                Exempt.LEVITATION, Exempt.FLIGHT) || data.position.sinceEntityCramTicks < 200;
        double max = 1.45;
        if (data.velocity.sinceVelocityAppliedTicks < 30) max += data.velocity.maxVelocityY;
        if (data.velocity.sinceExplosionTicks < 30) max += data.velocity.lastExplosionY;
        if (data.action.jumpBoostAmplifier > 0) max += data.action.jumpBoostAmplifier * 0.125;
        int riptide = data.position.sinceRiptideTicks;
        if (dy > max && pitch > -20.0f && !exempt && riptide >= 400) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " pitch=" + pitch + " rt=" + riptide);
        } else {
            decreaseBuffer();
        }
    }
}
