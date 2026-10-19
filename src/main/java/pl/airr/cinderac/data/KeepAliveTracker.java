package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.PacketEvents;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.util.Text;

/** Ping from server keepalives, delays between movement packets, kicks for missing keepalives and excessive ping. */
public final class KeepAliveTracker {
    private final PlayerData data;

    public int ping;
    public long lastFlyingMillis, flyingDelayMillis, lastKeepAliveMillis = System.currentTimeMillis();
    public long lastFastFlyingMillis = System.currentTimeMillis();
    public boolean fastFlying;
    private double timeoutBuffer;
    private boolean timeoutKicked, pingKicked;
    private int highPingTicks;

    public KeepAliveTracker(PlayerData data) {
        this.data = data;
    }

    public void handleFlying() {
        long now = System.currentTimeMillis();
        flyingDelayMillis = now - lastFlyingMillis;
        fastFlying = flyingDelayMillis <= 5L;
        if (fastFlying) lastFastFlyingMillis = System.currentTimeMillis();
        ping = data.player == null ? 0 : serverPing();
        lastFlyingMillis = now;
        checkTimeout();
        checkMaxPing();
    }

    private int serverPing() {
        try {
            return PacketEvents.getAPI().getPlayerManager().getPing(data.player);
        } catch (Exception e) {
            return 0;
        }
    }

    public void handleKeepAlive() {
        lastKeepAliveMillis = System.currentTimeMillis();
    }

    private void checkTimeout() {
        Settings s = Settings.get();
        if (data.player == null || !s.keepAliveKick || data.player.hasPermission("cinderac.bypass.connection.keepalive")) return;
        long delay = System.currentTimeMillis() - lastKeepAliveMillis;
        if (delay <= s.keepAliveMaxDelay || timeoutKicked) return;
        if (++timeoutBuffer > 5.0) {
            data.kick(s.keepAliveKickMessage);
            String d = Long.toString(delay);
            if (!s.keepAliveStaffMessage.isEmpty()) CinderAC.get().alerts().staff(s.keepAliveStaffMessage.replace("%delay%", d).replace("%player%", data.name));
            if (!s.keepAliveConsoleMessage.isEmpty()) CinderAC.get().getLogger().info(Text.strip(s.keepAliveConsoleMessage.replace("%player%", data.name).replace("%delay%", d)));
            timeoutKicked = true;
        } else if (timeoutBuffer > 0.0) {
            timeoutBuffer -= 0.1;
        }
    }

    private void checkMaxPing() {
        Settings s = Settings.get();
        if (data.player == null || !s.maxPingKick || data.player.hasPermission("cinderac.bypass.connection.max-ping")) return;
        if (ping > s.maxPing && !pingKicked) {
            if (++highPingTicks > s.maxPingTicks) {
                data.kick(s.maxPingKickMessage);
                String p = Long.toString(ping);
                if (!s.maxPingStaffMessage.isEmpty()) CinderAC.get().alerts().staff(s.maxPingStaffMessage.replace("%ping%", p).replace("%player%", data.name));
                if (!s.maxPingConsoleMessage.isEmpty()) CinderAC.get().getLogger().info(Text.strip(s.maxPingConsoleMessage.replace("%player%", data.name).replace("%ping%", p)));
                pingKicked = true;
            }
        } else if (highPingTicks > 0) {
            --highPingTicks;
        }
    }
}
