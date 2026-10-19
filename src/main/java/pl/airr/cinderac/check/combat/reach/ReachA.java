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

@CheckInfo(name = "Reach", type = 'A', complexType = "History", description = "Hit from too far away.")
public final class ReachA extends Check {
    public ReachA(PlayerData data) {
        super(data);
    }

    private boolean attacked = true;

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
        double dist = -1.0;
        boolean found = false;
        for (CombatTracker.TickedLocation t : data.combat.targetHistory) {
            if (Math.abs(now - t.tick - ping) >= 3) continue;
            double d = me.distance(t.location.toVector().setY(0)) - 0.52;
            dist = found ? Math.min(dist, d) : d;
            found = true;
        }
        if (dist == -1.0) {
            decreaseBufferBy(0.025);
        } else {
            PlayerData t = CinderAC.get().players().get(data.combat.target);
            if (t == null || t.player == null || t.isBedrock()) return;
            boolean knockback = data.player != null && data.player.getItemInHand().getEnchantmentLevel(org.bukkit.enchantments.Enchantment.KNOCKBACK) > 0;
            boolean exempt = exempt(Exempt.CREATIVE, Exempt.VEHICLE, Exempt.BOAT);
            boolean targetExempt = t.exempt(Exempt.VEHICLE, Exempt.BOAT);
            double max = data.action.interactionRange + (cfg.getDouble("max-reach", 3.03) - 3.0);
            if (System.currentTimeMillis() - data.keepAlive.lastFastFlyingMillis < 1500L) max += 1.15;
            if (exempt(Exempt.LIQUID)) max += 0.1;
            if (dist > max && dist < 8.0 && !knockback && !exempt && !targetExempt) {
                if (increaseBuffer() > maxBuffer()) fail("distance=" + dist);
            } else {
                decreaseBuffer();
            }
        }
        attacked = false;
    }
}
