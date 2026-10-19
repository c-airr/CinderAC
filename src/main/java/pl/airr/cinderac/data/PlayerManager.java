package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckRegistry;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Players tracked by CinderAC (by UUID from packetevents). */
public final class PlayerManager {
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final Map<UUID, String> brands = new ConcurrentHashMap<>();
    /** Vehicle checks read entities outside the region thread - disabled on Folia (as in the original). */
    private static final java.util.Set<String> NOT_ON_FOLIA = new java.util.HashSet<>(java.util.Arrays.asList(
            "BoatFlyB", "BoatFlyC", "EntityFlightA", "EntityFlightB", "EntitySpeedA", "NoSaddleA"));

    public PlayerData get(Player p) {
        return p == null ? null : players.get(p.getUniqueId());
    }

    public PlayerData get(UUID id) {
        return players.get(id);
    }

    public PlayerData getOrCreate(User user) {
        if (user == null || user.getUUID() == null) return null;
        return players.computeIfAbsent(user.getUUID(), id -> create(user));
    }

    private PlayerData create(User user) {
        PlayerData data = new PlayerData(user);
        addChecks(data);
        return data;
    }

    /** Creates instances of enabled checks (called again after a reload). */
    public void addChecks(PlayerData data) {
        data.checks.clear();
        for (Class<? extends Check> c : CheckRegistry.CHECKS) {
            if (pl.airr.cinderac.platform.ServerVersion.isFolia() && NOT_ON_FOLIA.contains(c.getSimpleName())) continue;
            try {
                Constructor<? extends Check> ctor = c.getConstructor(PlayerData.class);
                Check check = ctor.newInstance(data);
                if (check.cfg.enabled && !CinderAC.get().isDisabled(check.key)) data.checks.add(check);
            } catch (ReflectiveOperationException e) {
                CinderAC.get().getLogger().warning("Could not create " + c.getSimpleName() + ": " + e);
            }
        }
    }

    public void remove(UUID id) {
        players.remove(id);
        brands.remove(id);
    }

    public Collection<PlayerData> all() {
        return players.values();
    }

    public void putBrand(UUID id, String brand) {
        brands.put(id, brand);
    }

    public String takeBrand(UUID id) {
        return brands.remove(id);
    }
}
