package pl.airr.cinderac.platform;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Folia schedulers. This class is only loaded on Folia (ServerVersion.isFolia()). */
public final class FoliaTasks implements Tasks {
    private final Plugin plugin;

    public FoliaTasks(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runGlobal(Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public void runForPlayer(Player player, Runnable task) {
        player.getScheduler().run(plugin, t -> task.run(), null);
    }

    @Override
    public Cancellable repeatForPlayer(Player player, Runnable task) {
        ScheduledTask t = player.getScheduler().runAtFixedRate(plugin, st -> task.run(), null, 1L, 1L);
        return t == null ? () -> { } : t::cancel;
    }

    @Override
    public Cancellable repeatGlobal(long periodTicks, Runnable task) {
        ScheduledTask t = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, st -> task.run(), periodTicks, periodTicks);
        return t::cancel;
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }
}
