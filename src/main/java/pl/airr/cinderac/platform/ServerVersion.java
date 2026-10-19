package pl.airr.cinderac.platform;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server version, detected once at startup. Supports 1.8.8 up to the latest (including the 26.x numbering).
 * CinderAC compiles against the modern API - newer methods may only be called behind these guards.
 */
public final class ServerVersion {
    private static int major = 1, minor = 8, patch = 8;
    private static boolean folia;

    private ServerVersion() {
    }

    public static void detect() {
        Matcher m = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(Bukkit.getBukkitVersion());
        if (m.find()) {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
            patch = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        }
        folia = classExists("io.papermc.paper.threadedregions.RegionizedServer");
    }

    /** Whether the server is at least 1.minor.patch (26.x counts as newer than 1.x). */
    public static boolean atLeast(int minor, int patch) {
        if (major > 1) return true;
        return ServerVersion.minor > minor || (ServerVersion.minor == minor && ServerVersion.patch >= patch);
    }

    public static boolean atLeast(int minor) {
        return atLeast(minor, 0);
    }

    public static boolean isFolia() {
        return folia;
    }

    public static String describe() {
        return major + "." + minor + "." + patch + (folia ? " (Folia)" : "");
    }

    public static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
