package pl.airr.cinderac.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.data.PlayerData;

/** Events from newer versions - registered only on 1.13+ (these classes do not exist on 1.8). */
public final class ModernListener implements Listener {
    private final CinderAC plugin;

    public ModernListener(CinderAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRiptide(PlayerRiptideEvent e) {
        PlayerData d = plugin.players().get(e.getPlayer());
        if (d == null) return;
        synchronized (d) {
            d.position.sinceRiptideTicks = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResurrect(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        PlayerData d = plugin.players().get((Player) e.getEntity());
        if (d == null) return;
        synchronized (d) {
            d.action.sinceTotemTicks = 0;
        }
    }
}
