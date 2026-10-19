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

@CheckInfo(name = "Hitbox", type = 'B', complexType = "Simple", description = "Attacked while not looking at target.")
public final class HitboxB extends Check {
    public HitboxB(PlayerData data) {
        super(data);
    }

    private boolean attacked;
    // in the original the target is never assigned, so the check never flags - kept 1:1
    private Player target;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK || !(data.combat.lastAttacked instanceof Player)) return;
            attacked = true;
            return;
        }
        if (!p.isFlying() || !attacked) return;
        if (target != null) {
            PlayerData t = CinderAC.get().players().get(target);
            if (t == null) return;
            double dx = t.position.x - data.position.x, dz = t.position.z - data.position.z;
            float yaw = data.rotation.yaw;
            Vector look = new Vector(-Math.sin(yaw * (float) Math.PI / 180.0f) * 1.0 * 0.5, 0.0, Math.cos(yaw * (float) Math.PI / 180.0f) * 1.0 * 0.5);
            double reach = data.combat.reachDistance;
            double angle = Math.toDegrees(new Vector(dx, 0.0, dz).angle(look));
            double max = cfg.getDouble("max-angle", 45);
            boolean targetExempt = t.exempt(Exempt.VEHICLE, Exempt.BOAT);
            boolean exempt = exempt(Exempt.CREATIVE, Exempt.VEHICLE, Exempt.DEAD);
            if (reach < 1.0) max += 0.45;
            double minReach = ServerVersion.atLeast(9) ? 0.3 : 0.8;
            if (angle > max && reach > minReach && reach < 10.0 && !exempt && !targetExempt) {
                if (increaseBuffer() > maxBuffer()) fail("angle=" + angle + " distance=" + reach);
            } else {
                decreaseBuffer();
            }
        }
        attacked = false;
    }
}
