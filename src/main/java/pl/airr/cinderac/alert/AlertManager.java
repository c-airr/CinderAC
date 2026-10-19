package pl.airr.cinderac.alert;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.config.CheckConfig;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/** VL, staff alerts, violations.txt / punishments.txt logging and punishments. */
public final class AlertManager {
    private final CinderAC plugin;
    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "CinderAC-log");
        t.setDaemon(true);
        return t;
    });
    private static final ThreadLocal<DecimalFormat> COORD = ThreadLocal.withInitial(() -> new DecimalFormat("#.##"));
    private static final ThreadLocal<SimpleDateFormat> DATE = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public AlertManager(CinderAC plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player p) {
        if (!enabled.remove(p.getUniqueId())) {
            enabled.add(p.getUniqueId());
            return true;
        }
        return false;
    }

    public void enable(Player p) {
        enabled.add(p.getUniqueId());
    }

    public void remove(Player p) {
        enabled.remove(p.getUniqueId());
    }

    /** Message to staff members with alerts enabled. */
    public void staff(String message) {
        if (message == null || message.isEmpty()) return;
        String colored = Text.color(Settings.get().withPrefix(message));
        for (UUID id : enabled) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage(colored);
        }
    }

    /** Called from Check.fail() while holding the player lock. */
    public void handleFail(Check check, PlayerData data, String info) {
        Settings s = Settings.get();
        CheckConfig cfg = check.cfg;
        Player p = data.player;
        if (!cfg.enabled || p == null || !p.isOnline()) return;
        if (System.currentTimeMillis() - data.lastPunishTime < s.punishmentDelay) return;
        if (plugin.tps() < cfg.minimumTps) return;
        if (data.keepAlive.ping > cfg.maximumPing) return;
        if (p.hasPermission("cinderac.bypass") || p.hasPermission("cinderac.bypass." + check.key.toLowerCase())) return;
        if (data.isBedrock()) return;
        if (check.category.equals("movement")) data.position.sinceMovementFlagTicks = 0;
        String nameType = (check.info.name() + check.info.type()).toLowerCase().replace(" ", "");
        if (s.cancelHitsAll || s.cancelHitsChecks.contains(nameType)) data.lastFlagTime = System.currentTimeMillis();

        check.setVl(check.vl() + 1);
        int vl = check.vl();
        String lower = check.info.name().toLowerCase().replace(" ", "");
        String nameColonType = lower + ":" + Character.toLowerCase(check.info.type());
        if (!check.info.experimental() && !nameColonType.equals("invalid:a") && !nameColonType.equals("invalid:b")) {
            if (!lower.equals("timer")) data.totalViolations++;
            if (lower.equals("timer")) data.timerViolations++;
            if (lower.equals("autoclicker")) data.autoClickerViolations++;
            if (lower.equals("scaffold")) data.scaffoldViolations++;
            switch (check.category) {
                case "combat":
                    if (!lower.equals("autoclicker")) data.combatViolations++;
                    break;
                case "movement":
                    data.movementViolations++;
                    break;
                default:
                    if (!lower.equals("timer")) data.playerViolations++;
                    break;
            }
        }

        if (vl % cfg.alertInterval == 0 && vl >= cfg.dontAlertUntil) {
            String severity = severity(vl, data.totalViolations);
            if (s.logEnabled) log("violations.txt", fill(s.logAlertMessage, check, data, info, severity));
            boolean underCap = !s.ignoreAlertsOverMax || vl <= cfg.maxViolations + s.ignoreAlertsOverMaxAmount;
            if (s.printToConsole && underCap) plugin.getLogger().info(Text.strip(fill(s.consoleFormat, check, data, info, severity)));
            if (!enabled.isEmpty() && underCap) sendAlert(check, data, info, severity);
            for (String cmd : s.customCommands) {
                String c = fill(cmd, check, data, info, severity);
                plugin.tasks().runGlobal(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c));
            }
        }
        if (cfg.punishable && !check.info.experimental() && vl >= cfg.maxViolations && p.isOnline()) punish(check, data);
    }

    private void sendAlert(Check check, PlayerData data, String info, String severity) {
        Settings s = Settings.get();
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(Text.color(fill(s.alertFormat, check, data, info, severity))));
        if (!s.clickCommands.isEmpty()) {
            String cmd = s.clickCommands.get(s.clickCommands.size() - 1).replace("%player%", data.name)
                    .replace("%uuid%", data.uuid.toString());
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        }
        String hover = fill(String.join("\n", s.hover), check, data, info, severity);
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Text.color(hover))));
        BaseComponent[] out = {msg};
        for (UUID id : enabled) {
            Player staff = Bukkit.getPlayer(id);
            if (staff != null) staff.spigot().sendMessage(out);
        }
    }

    private String severity(int vl, int total) {
        Settings s = Settings.get();
        int value = s.perCheckSeverities ? vl : total;
        int level = 1;
        for (int i = 2; i <= 10; i++) if (value >= s.severityThresholds[i]) level = i;
        return Text.color(s.severityColors[level]);
    }

    /** Fills in all alert placeholders. */
    public String fill(String template, Check check, PlayerData data, String info, String severity) {
        Settings s = Settings.get();
        Player p = data.player;
        DecimalFormat f = COORD.get();
        String dev = check.info.experimental() ? s.experimentalSymbol : "";
        String opponent = data.combat.target == null ? "None" : data.combat.target.getName();
        return s.withPrefix(template)
                .replace("%player%", data.name)
                .replace("%uuid%", data.uuid.toString())
                .replace("%max-vl%", Integer.toString(check.cfg.maxViolations))
                .replace("%check%", check.info.name())
                .replace("%info%", info)
                .replace("%server-name%", "Default")
                .replace("%cps%", f.format(data.clicks.cps))
                .replace("%category%", capitalize(check.category))
                .replace("%complex-type%", check.info.complexType())
                .replace("%client-brand%", data.clientBrand)
                .replace("%x%", f.format(data.position.x))
                .replace("%y%", f.format(data.position.y))
                .replace("%z%", f.format(data.position.z))
                .replace("%opponent%", opponent)
                .replace("%opponennt%", opponent)
                .replace("%world%", p == null ? "?" : p.getWorld().getName())
                .replace("%total-violations%", Integer.toString(data.totalViolations))
                .replace("%combat-violations%", Integer.toString(data.combatViolations))
                .replace("%movement-violations%", Integer.toString(data.movementViolations))
                .replace("%player-violations%", Integer.toString(data.playerViolations))
                .replace("%ping%", Integer.toString(data.keepAlive.ping))
                .replace("%description%", check.info.description())
                .replace("%tps%", f.format(plugin.tps()))
                .replace("%version%", data.clientVersion().getReleaseName())
                .replace("%client-version%", data.clientVersion().getReleaseName())
                .replace("%dev%", dev)
                .replace("%severity%", severity)
                .replace("%vl%", Integer.toString(check.vl()))
                .replace("%type%", Character.toString(check.info.type()));
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void punish(Check check, PlayerData data) {
        Settings s = Settings.get();
        CheckConfig cfg = check.cfg;
        Player p = data.player;
        if (!cfg.punishable || p == null) return;
        if (System.currentTimeMillis() - data.lastPunishTime < s.punishmentDelay) return;
        data.lastPunishTime = System.currentTimeMillis();
        if (p.hasPermission("cinderac.bypass." + check.key.toLowerCase())) return;
        String severity = severity(check.vl(), data.totalViolations);
        if (s.punishmentFileEnabled) log("punishments.txt", fill(s.logPunishmentMessage, check, data, "", severity));
        int id = ThreadLocalRandom.current().nextInt(50000);
        if (!s.punishMessage.isEmpty()) staff(fill(s.punishMessage, check, data, "", severity).replace("%punishment-id%", Integer.toString(id)));
        if (!s.punishmentsEnabled) {
            staff("%prefix% &7(punishments disabled: settings.punishments-enabled = false)");
        } else {
            if (cfg.broadcastPunishment) {
                for (String line : s.punishBroadcast) {
                    String l = Text.color(fill(line, check, data, "", severity));
                    plugin.tasks().runGlobal(() -> Bukkit.broadcastMessage(l));
                }
            }
            for (String cmd : cfg.punishmentCommands) {
                if (cmd.toLowerCase().startsWith("bungee:")) continue;
                String c = Text.color(fill(cmd, check, data, "", severity).replace("%punishment-id%", Integer.toString(id)));
                plugin.tasks().runGlobal(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c));
            }
        }
        for (Check c : data.checks) c.setVl(0);
    }

    public void log(String file, String line) {
        String stamped = "[" + DATE.get().format(new Date()) + "] " + Text.strip(line);
        File target = new File(plugin.getDataFolder(), file);
        io.execute(() -> {
            try {
                target.getParentFile().mkdirs();
                try (Writer w = new OutputStreamWriter(new FileOutputStream(target, true), StandardCharsets.UTF_8)) {
                    w.write(stamped);
                    w.write(System.lineSeparator());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not write " + file + ": " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        io.shutdown();
    }
}
