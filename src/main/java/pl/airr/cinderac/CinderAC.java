package pl.airr.cinderac;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.airr.cinderac.alert.AlertManager;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.command.CinderCommand;
import pl.airr.cinderac.config.CheckConfig;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PlayerManager;
import pl.airr.cinderac.listener.BukkitListener;
import pl.airr.cinderac.listener.ModernListener;
import pl.airr.cinderac.listener.PacketListener;
import pl.airr.cinderac.platform.BukkitTasks;
import pl.airr.cinderac.platform.FoliaTasks;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.platform.Tasks;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/** Main plugin class. Author: airr_ */
public final class CinderAC extends JavaPlugin {
    private static CinderAC instance;
    private final AtomicInteger tick = new AtomicInteger();
    private final PlayerManager players = new PlayerManager();
    private final Map<String, CheckConfig> checkConfigs = new ConcurrentHashMap<>();
    private final Set<String> disabledChecks = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Integer> fishedBoats = new ConcurrentHashMap<>();
    private final Set<String> loggedErrors = ConcurrentHashMap.newKeySet();
    private AlertManager alerts;
    private Tasks tasks;
    private PacketListener packetListener;
    private long enableTime;
    private int minutesSinceReset;

    public static CinderAC get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        instance = this;
        enableTime = System.currentTimeMillis();
        ServerVersion.detect();
        tasks = ServerVersion.isFolia() ? new FoliaTasks(this) : new BukkitTasks(this);
        alerts = new AlertManager(this);
        saveDefaultConfig();
        loadConfiguration();
        getServer().getPluginManager().registerEvents(new BukkitListener(this), this);
        if (ServerVersion.atLeast(13)) getServer().getPluginManager().registerEvents(new ModernListener(this), this);
        packetListener = new PacketListener(this);
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        PluginCommand cmd = getCommand("cinderac");
        if (cmd != null) {
            CinderCommand handler = new CinderCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
        tasks.repeatGlobal(1L, this::onTick);
        getLogger().info("CinderAC enabled on " + ServerVersion.describe() + ", checks: " + pl.airr.cinderac.check.CheckRegistry.CHECKS.size());
    }

    @Override
    public void onDisable() {
        if (packetListener != null) PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        if (alerts != null) alerts.shutdown();
    }

    /** Every tick: tick counter, one transaction per player, periodic VL reset. */
    private void onTick() {
        int t = tick.incrementAndGet();
        for (PlayerData d : players.all()) {
            try {
                d.transactions.sendConfirmation(true);
            } catch (RuntimeException ignored) {
                // the channel may already be closed
            }
        }
        Settings s = Settings.get();
        if (s.violationResetEnabled && t % 1200 == 0 && ++minutesSinceReset >= s.violationResetMinutes) {
            minutesSinceReset = 0;
            resetViolations();
            if (s.violationResetMessageEnabled) alerts.staff(s.violationResetMessage);
        }
    }

    public void resetViolations() {
        for (PlayerData d : players.all()) resetViolations(d);
    }

    public void resetViolations(PlayerData d) {
        synchronized (d) {
            for (Check c : d.checks) c.setVl(0);
            d.totalViolations = d.combatViolations = d.movementViolations = d.playerViolations = 0;
            d.autoClickerViolations = d.scaffoldViolations = d.timerViolations = 0;
        }
    }

    public void loadConfiguration() {
        reloadConfig();
        Settings.load(getConfig());
        checkConfigs.clear();
        disabledChecks.clear();
        YamlConfiguration defaults = new YamlConfiguration();
        try (InputStreamReader r = new InputStreamReader(getResource("checks.yml"), StandardCharsets.UTF_8)) {
            defaults.load(r);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Missing default checks.yml", e);
        }
        File file = new File(getDataFolder(), "checks.yml");
        if (!file.exists()) saveResource("checks.yml", false);
        YamlConfiguration checks = YamlConfiguration.loadConfiguration(file);
        checks.setDefaults(defaults);
        checksSection = checks;
        for (PlayerData d : players.all()) {
            synchronized (d) {
                players.addChecks(d);
            }
        }
    }

    private ConfigurationSection checksSection;

    public CheckConfig checkConfig(String path) {
        return checkConfigs.computeIfAbsent(path, p -> new CheckConfig(checksSection == null ? null : checksSection.getConfigurationSection(p)));
    }

    public boolean isDisabled(String key) {
        return disabledChecks.contains(key);
    }

    public void disableCheck(String key) {
        disabledChecks.add(key);
        for (PlayerData d : players.all()) {
            synchronized (d) {
                d.checks.removeIf(c -> c.key.equals(key));
            }
        }
    }

    public int tick() {
        return tick.get();
    }

    public double tps() {
        if (ServerVersion.isFolia()) return 20.0;
        try {
            return Math.min(20.0, SpigotReflectionUtil.getTPS());
        } catch (RuntimeException e) {
            return 20.0;
        }
    }

    public long enableTime() {
        return enableTime;
    }

    public PlayerManager players() {
        return players;
    }

    public AlertManager alerts() {
        return alerts;
    }

    public Tasks tasks() {
        return tasks;
    }

    public void markFishedBoat(int entityId) {
        fishedBoats.put(entityId, tick());
    }

    public Integer fishedBoatTick(int entityId) {
        return fishedBoats.get(entityId);
    }

    public void debug(PlayerData d, Check c, String msg) {
        alerts.staff("&8[debug] &7" + d.name + " " + c.key + ": " + msg);
    }

    /** An exception in a check must not break packet processing - logged once per class. */
    public void checkError(Check c, RuntimeException e) {
        if (loggedErrors.add(c.key + e.getClass().getName())) {
            getLogger().log(Level.WARNING, "Error in check " + c.key, e);
        }
    }
}
