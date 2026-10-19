package pl.airr.cinderac.data;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.util.Samples;

/** Combat: last target, target position history (from entity movement packets) and distance to it. */
public final class CombatTracker {
    private final PlayerData data;

    public int hitTicks = 100, lastAttackTick, lastSwingTick, lastItemFrameTick;
    public Entity lastAttacked;
    public Player target, lastTarget;
    public double targetDistance, reachDistance;
    public final Samples<TickedLocation> targetHistory = new Samples<>(20);

    public CombatTracker(PlayerData data) {
        this.data = data;
    }

    public void handleInteract(WrapperPlayClientInteractEntity w) {
        Player p = data.player;
        if (p == null) return;
        Entity entity = entityById(p.getWorld(), w.getEntityId());
        if (entity instanceof ItemFrame) lastItemFrameTick = CinderAC.get().tick();
        if (w.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
        lastAttacked = entity;
        if (lastAttacked != null) {
            targetDistance = p.getLocation().toVector().setY(0).distance(lastAttacked.getLocation().toVector().setY(0)) - 0.42;
        }
        if (entity instanceof Player) {
            lastTarget = target == null ? (Player) entity : target;
            target = (Player) entity;
            if (target != lastTarget) targetHistory.clear();
        }
        hitTicks = 0;
    }

    public static Entity entityById(World world, int id) {
        try {
            return SpigotReflectionUtil.getEntityById(world, id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Entity movement sent to the player; for the target we store its position before the move. */
    public void handleEntityMove(int entityId, double dx, double dy, double dz) {
        if (target == null || target.getEntityId() != entityId) return;
        PlayerData td = CinderAC.get().players().get(target);
        if (td == null || td.player == null) return;
        World world = td.player.getWorld();
        Location loc = new Location(world, td.position.x - dx, td.position.y - dy, td.position.z - dz);
        targetHistory.add(new TickedLocation(loc, CinderAC.get().tick()));
    }

    public void clearHistory() {
        targetHistory.clear();
    }

    public void onAttackEvent() {
        lastAttackTick = CinderAC.get().tick();
    }

    public void onDamagedEvent() {
        lastSwingTick = CinderAC.get().tick();
    }

    public int ticksSinceAttack() {
        return CinderAC.get().tick() - lastAttackTick;
    }

    public int ticksSinceSwing() {
        return CinderAC.get().tick() - lastSwingTick;
    }

    public void handleFlying() {
        ++hitTicks;
        if (targetHistory.size() > 0 && target != null) {
            int now = CinderAC.get().tick();
            int pingTicks = data.pingTicks();
            Vector me = new Vector(data.position.x, 0.0, data.position.z);
            double min = Double.MAX_VALUE;
            boolean found = false;
            for (TickedLocation t : targetHistory) {
                if (Math.abs(now - t.tick - pingTicks) >= 3) continue;
                double d = me.distance(t.location.toVector().setY(0)) - 0.52;
                if (d < min) min = d;
                found = true;
            }
            reachDistance = found ? min : -1.0;
        }
    }

    public static final class TickedLocation {
        public final Location location;
        public final int tick;

        public TickedLocation(Location location, int tick) {
            this.location = location;
            this.tick = tick;
        }
    }
}
