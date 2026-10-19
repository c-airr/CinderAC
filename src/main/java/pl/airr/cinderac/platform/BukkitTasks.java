package pl.airr.cinderac.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Classic Bukkit scheduler (Spigot/Paper/Purpur without Folia). */
public final class BukkitTasks implements Tasks {
    private final Plugin plugin;

    public BukkitTasks(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runGlobal(Runnable task) {
        if (Bukkit.isPrimaryThread()) task.run();
        else Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runForPlayer(Player player, Runnable task) {
        runGlobal(task);
    }

    @Override
    public Cancellable repeatForPlayer(Player player, Runnable task) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, task, 1L, 1L);
        return t::cancel;
    }

    @Override
    public Cancellable repeatGlobal(long periodTicks, Runnable task) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, task, periodTicks, periodTicks);
        return t::cancel;
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
