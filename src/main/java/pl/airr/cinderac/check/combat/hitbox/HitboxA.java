package pl.airr.cinderac.check.combat.hitbox;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.CombatTracker;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Hitbox", type = 'A', complexType = "History", description = "Attacked while not looking at target.")
public final class HitboxA extends Check {
    public HitboxA(PlayerData data) {
        super(data);
    }

    private boolean attacked;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK || !(data.combat.lastAttacked instanceof Player)) return;
            attacked = true;
            return;
        }
        if (!p.isFlying() || !attacked) return;
        int now = tick(), ping = data.pingTicks();
        Vector me = new Vector(data.position.x, 0.0, data.position.z);
        Vector look = look(data.rotation.yaw, data.rotation.pitch).setY(0.0);
        double angle = -1.0;
        boolean found = false;
        for (CombatTracker.TickedLocation t : data.combat.targetHistory) {
            if (Math.abs(now - t.tick - ping) >= 3) continue;
            double a = t.location.toVector().setY(0.0).subtract(me).angle(look);
            angle = found ? Math.min(angle, a) : a;
            found = true;
        }
        double reach = data.combat.reachDistance;
        if (angle == -1.0) {
            decreaseBufferBy(0.025);
        } else {
            PlayerData t = CinderAC.get().players().get(data.combat.target);
            if (t == null) return;
            boolean targetExempt = t.exempt(Exempt.VEHICLE, Exempt.BOAT);
            double max = cfg.getDouble("max-angle", 0.36);
            if (System.currentTimeMillis() - data.keepAlive.lastFastFlyingMillis < 1500L) max += 0.25;
            boolean exempt = exempt(Exempt.CREATIVE, Exempt.GLIDING, Exempt.VEHICLE);
            double minReach = ServerVersion.atLeast(9) ? 0.25 : 0.75;
            if (angle > max && reach > minReach && reach < 10.0 && !exempt && !targetExempt) {
                if (increaseBuffer() > maxBuffer()) {
                    fail("angle=" + angle + " distance=" + reach + " dYaw=" + data.rotation.deltaYaw + " dXZ=" + data.position.deltaXZ);
                }
            } else {
                decreaseBuffer();
            }
        }
        attacked = false;
    }

    private static Vector look(float yaw, float pitch) {
        Vector v = new Vector();
        v.setY(-Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        v.setX(-xz * Math.sin(Math.toRadians(yaw)));
        v.setZ(xz * Math.cos(Math.toRadians(yaw)));
        return v;
    }
}
