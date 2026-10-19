package pl.airr.cinderac.check.combat.velocity;

import com.github.retrooper.packetevents.util.Vector3d;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

import java.util.List;

@CheckInfo(name = "Velocity", type = 'A', complexType = "Vertical", description = "Vertical velocity modifications.")
public final class VelocityA extends Check {
    public VelocityA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (data.position.nearWorldBorder || !data.velocity.hasPendingVelocity) return;
        double dy = data.position.deltaY;
        if (Math.abs(dy - data.action.jumpStrengthAttribute) < 0.001) return;
        List<Vector3d> pending = data.velocity.pendingVelocities;
        for (Vector3d v : pending) if (v.getY() <= 0.0) return;
        int ticks = data.velocity.sinceVelocityTicks;
        boolean exempt = exempt(Exempt.COLLIDING_VERTICALLY, Exempt.LIQUID, Exempt.FROZEN, Exempt.CHUNK, Exempt.FALL_DAMAGE, Exempt.ENDER_PEARL,
                Exempt.WEB, Exempt.VOID, Exempt.SLOW_FALLING, Exempt.TRAPDOOR, Exempt.SOUL_SAND, Exempt.SLIME, Exempt.COMBO_MODE, Exempt.FLIGHT,
                Exempt.VEHICLE, Exempt.MYTHIC_MOB, Exempt.ELYTRA, Exempt.FENCE, Exempt.WALL, Exempt.FIREBALL, Exempt.SWEET_BERRIES, Exempt.PLACED_WEB);
        double min = 0.999;
        if (exempt(Exempt.COLLIDING_HORIZONTALLY)) min -= 0.1;
        if (exempt(Exempt.NETHERITE_ARMOR)) min -= 0.1;
        double ratio = 0.0, velocity = 0.0;
        boolean taken = false;
        for (Vector3d v : pending) {
            ratio = dy / v.getY();
            velocity = v.getY();
            if (ratio >= min) {
                taken = true;
                break;
            }
        }
        if (!taken && !exempt) {
            if (increaseBuffer() > maxBuffer()) {
                fail("percent=" + ratio * 100.0 + "% deltaY=" + dy + " velocity=" + velocity + " size=" + pending.size() + " ticks=" + ticks);
            }
        } else {
            decreaseBuffer();
        }
    }
}
