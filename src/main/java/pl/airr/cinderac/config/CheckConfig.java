package pl.airr.cinderac.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Settings of a single check (checks.<category>.<name>.<type>). */
public final class CheckConfig {
    public final boolean enabled, punishable, broadcastPunishment, setback, hotbarShuffle, randomRotation;
    public final int maxViolations, alertInterval, dontAlertUntil, maximumPing, minVlToSetback;
    public final int shuffleMinVl, shuffleEvery, rotateMinVl, rotateEvery;
    public final double minimumTps, bufferMax, bufferMultiple, bufferDecay;
    public final List<String> punishmentCommands;
    private final ConfigurationSection section;

    public CheckConfig(ConfigurationSection s) {
        if (s == null) s = new MemoryConfiguration();
        section = s;
        enabled = s.getBoolean("enabled", true);
        punishable = s.getBoolean("punishable", false);
        broadcastPunishment = s.getBoolean("broadcast-punishment", false);
        maxViolations = s.getInt("max-violations", 10);
        alertInterval = Math.max(1, s.getInt("alert-interval", 1));
        dontAlertUntil = s.getInt("dont-alert-until", 1);
        maximumPing = s.getInt("maximum-ping", 100000);
        minimumTps = s.getDouble("minimum-tps", -1);
        punishmentCommands = Collections.unmodifiableList(new ArrayList<>(s.getStringList("punishment-commands")));
        // buffer.max read as an integer (1.5 -> 1), as in the original
        bufferMax = s.getInt("buffer.max", 1);
        bufferMultiple = s.getDouble("buffer.multiple", 0.5);
        bufferDecay = s.getDouble("buffer.decay", 0.1);
        hotbarShuffle = s.getBoolean("hotbar-shuffle.enabled", false);
        shuffleMinVl = s.getInt("hotbar-shuffle.minimum-vl-to-shuffle", 2);
        shuffleEvery = Math.max(1, s.getInt("hotbar-shuffle.shuffle-every", 2));
        randomRotation = s.getBoolean("random-rotation.enabled", false);
        rotateMinVl = s.getInt("random-rotation.minimum-vl-to-randomly-rotate", 2);
        rotateEvery = Math.max(1, s.getInt("random-rotation.rotate-every", 2));
        setback = s.getBoolean("setback", false);
        minVlToSetback = s.getInt("min-vl-to-setback", 0);
    }

    public double getDouble(String key, double def) {
        return section.getDouble(key, def);
    }

    public int getInt(String key, int def) {
        return section.getInt(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return section.getBoolean(key, def);
    }
}
