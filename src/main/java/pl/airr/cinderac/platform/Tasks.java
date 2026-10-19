package pl.airr.cinderac.platform;

import org.bukkit.entity.Player;

/**
 * Platform-independent task scheduling: Bukkit (single main thread) or Folia (region threads).
 */
public interface Tasks {
    /** Task on the "global" thread (Bukkit: main, Folia: global region) - console commands, broadcasts. */
    void runGlobal(Runnable task);

    /** Task on the player owner thread (Bukkit: main, Folia: player region). */
    void runForPlayer(Player player, Runnable task);

    /** Per-tick repeating task on the player owner thread; returns a handle for cancelling. */
    Cancellable repeatForPlayer(Player player, Runnable task);

    /** Global task repeating every periodTicks. */
    Cancellable repeatGlobal(long periodTicks, Runnable task);

    void runAsync(Runnable task);

    interface Cancellable {
        void cancel();
    }
}
