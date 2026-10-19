package pl.airr.cinderac.check.combat.reach;

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

@CheckInfo(name = "Reach", type = 'B', complexType = "Simple", description = "Hit from too far away.")
public final class ReachB extends Check {
    public ReachB(PlayerData data) {
        super(data);
    }

    private boolean attacked;
    private Player target;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK || !(data.combat.lastAttacked instanceof Player)) {
                target = null;
                attacked = false;
                return;
            }
            attacked = true;
            target = (Player) data.combat.lastAttacked;
            return;
        }
        if (!p.isFlying() || !attacked || target == null) return;
        PlayerData t = CinderAC.get().players().get(target);
        if (t == null || t.player == null) return;
        double dy = Math.abs(data.position.y - t.position.y);
        double dist = new Vector(data.position.x, 0.0, data.position.z).distance(new Vector(t.position.x, 0.0, t.position.z)) - 0.565;
        // limit uses max-reach of type A (as in the original)
        double max = data.action.interactionRange + (CinderAC.get().checkConfig("checks.combat.reach.a").getDouble("max-reach", 3.03) - 3.0);
        max += 0.05;
        max += dy * 0.9;
        max += data.position.deltaXZ * 0.66;
        if (data.action.sprinting) max += 0.32;
        if (data.position.groundTicks < 3) max += 0.35;
        if (data.action.hasSpeed) max += (double) data.action.speedAmplifier * 0.125;
        double facing = data.player == null || t.player == null ? 101.0
                : Math.abs(180.0f - Math.abs(data.player.getLocation().getYaw() - t.player.getLocation().getYaw()));
        if (facing > 100.0) max += 1.25;
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.JOINED, Exempt.PLUGIN_LOAD, Exempt.WORLD_CHANGE, Exempt.CREATIVE, Exempt.VEHICLE);
        if (exempt(Exempt.SERVER_POSITION) || System.currentTimeMillis() - data.keepAlive.lastFastFlyingMillis < 1500L) max += 1.0;
        boolean targetExempt = t.exempt(Exempt.VEHICLE, Exempt.BOAT);
        if (dist > max && dist < 6.0 && !exempt && !targetExempt) {
            if (increaseBuffer() > maxBuffer()) fail(dist + " > " + max);
        } else {
            decreaseBuffer();
        }
        attacked = false;
    }
}
