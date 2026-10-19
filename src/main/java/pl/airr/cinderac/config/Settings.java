package pl.airr.cinderac.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Global settings from config.yml (immutable snapshot, replaced on reload). */
public final class Settings {
    private static volatile Settings current = new Settings(null);

    public final String prefix;

    public final String alertFormat, consoleFormat, toggledOn, toggledOff, experimentalSymbol;
    public final List<String> hover, clickCommands, clickAlertCommands, customCommands;
    public final boolean printToConsole;
    public final int[] severityThresholds = new int[11];
    public final String[] severityColors = new String[11];

    public final boolean logEnabled, punishmentFileEnabled;
    public final String logAlertMessage, logPunishmentMessage;

    public final boolean unloadedChunksSetback;
    public final String unloadedChunksMessage;
    public final int unloadedChunksMaxTicks;

    public final boolean violationResetEnabled, violationResetMessageEnabled;
    public final int violationResetMinutes;
    public final String violationResetMessage;

    public final boolean brandResolve, brandAlerts, brandConsole, brandWhitelist;
    public final List<String> brandIgnore, brandBlocked, brandWhitelisted;
    public final String brandMessage, brandKickMessage, brandConsoleMessage;

    public final boolean otherAnticheatSupport;
    public final String invalidKickMessage, invalidStaffMessage, invalidConsoleMessage;
    public final String outOfOrderKickMessage, outOfOrderStaffMessage, outOfOrderConsoleMessage;
    public final int noResponseDelaySeconds;
    public final boolean codeOfConductFix;
    public final String noResponseKickMessage, noResponseStaffMessage, noResponseConsoleMessage;
    public final int maxPendingTransactions;
    public final String maxSizeKickMessage, maxSizeStaffMessage, maxSizeConsoleMessage;
    public final boolean keepAliveKick;
    public final long keepAliveMaxDelay;
    public final String keepAliveKickMessage, keepAliveStaffMessage, keepAliveConsoleMessage;
    public final boolean maxPingKick;
    public final int maxPing, maxPingTicks;
    public final String maxPingKickMessage, maxPingStaffMessage, maxPingConsoleMessage;

    public final boolean cancelHitsAll, cancelHitsEvent;
    public final long cancelHitsDuration;
    public final List<String> cancelHitsChecks;

    public final String punishMessage;
    public final List<String> punishBroadcast;

    public final boolean ghostBlocksEnabled, ghostBlocksIgnoreLegacyVia, ghostBlocksSetback, ghostBlocksMessageEnabled;
    public final boolean ghostBlocksStaffMessageEnabled, ghostBlocksConsole, ghostWaterFix;
    public final double ghostBlocksMinTps, ghostBlocksBufferMax, ghostBlocksBufferDecay;
    public final int ghostBlocksTicks;
    public final String ghostBlocksMessage, ghostBlocksStaffMessage;

    public final boolean toggleAlertsOnJoin, perCheckSeverities, cinematic, ignoreGeyserUuids, ignoreGeyserPrefixes;
    public final String geyserPrefix;
    public final long punishmentDelay, joinCheckWaitTime;
    public final int flightCooldown, minTicksExisted, entityCramAmount, entityCramExemptTicks, ignoreAlertsOverMaxAmount;
    public final boolean lenientScaffolding, entityCollision, ignoreAlertsOverMax, dontSetbackLowerPositions;
    public final boolean velocityWorldBorder, entityCramFix, punishmentsEnabled;

    public final String msgNoPermission, msgInvalidTarget, msgReload, msgResetAll, msgResetPlayer, msgUnknown;

    private Settings(FileConfiguration c) {
        ConfigurationSection s = c == null ? new org.bukkit.configuration.MemoryConfiguration() : c;
        prefix = s.getString("prefix", "&c&lCinderAC &8»");
        alertFormat = str(s, "alerts.format", "%prefix% &f%player% &7failed &f%check% %dev%&7(&fType %type%&7)%dev% &7[&c%vl%&7/&c%max-vl%&7]");
        hover = list(s, "alerts.hover-message");
        clickCommands = list(s, "alerts.click-commands");
        clickAlertCommands = list(s, "alerts.click-alert-command-commands");
        toggledOn = str(s, "alerts.toggled-on-message", "%prefix% &7Alerty &awlaczone&7!");
        toggledOff = str(s, "alerts.toggled-off-message", "%prefix% &7Alerty &cwylaczone&7!");
        printToConsole = s.getBoolean("alerts.print-to-console", true);
        consoleFormat = str(s, "alerts.console-format", "[CinderAC] %player% failed %check% %dev%(Type %type%)%dev% (%vl%/%max-vl%)");
        customCommands = list(s, "alerts.custom-commands");
        int[] def = {0, 0, 4, 8, 12, 16, 16, 16, 16, 16, 16};
        for (int i = 2; i <= 10; i++) severityThresholds[i] = s.getInt("alerts.severity.violations." + i, def[i]);
        String[] defColors = {"", "&a", "&e", "&6", "&c", "&4", "&4", "&4", "&4", "&4", "&4"};
        for (int i = 1; i <= 10; i++) severityColors[i] = str(s, "alerts.severity.colors." + i, defColors[i]);
        experimentalSymbol = str(s, "alerts.experimental-symbol", "*");

        logEnabled = s.getBoolean("log-file.enabled", true);
        logAlertMessage = str(s, "log-file.alert-message", "%player% failed %check% %dev%(Type %type%)%dev% [VL: %vl%] [Ping: %ping%] [TPS: %tps%] [Version: %version%] [Client: %client-brand%]");
        logPunishmentMessage = str(s, "log-file.punishment-message", "%player% was punished for %check% %dev%(Type %type%)%dev% [VL: %vl%] [Ping: %ping%] [TPS: %tps%] [Client: %client-brand%]");
        punishmentFileEnabled = s.getBoolean("punishment-file.enabled", true);

        unloadedChunksSetback = s.getBoolean("unloaded-chunks.setback-enabled", false);
        unloadedChunksMessage = str(s, "unloaded-chunks.message", "");
        unloadedChunksMaxTicks = s.getInt("unloaded-chunks.max-ticks", 40);

        violationResetEnabled = s.getBoolean("violation-reset.enabled", true);
        violationResetMinutes = s.getInt("violation-reset.interval-in-minutes", 8);
        violationResetMessageEnabled = s.getBoolean("violation-reset.message-enabled", true);
        violationResetMessage = str(s, "violation-reset.message", "%prefix% &7Violations for all online players were reset!");

        brandResolve = s.getBoolean("client-brand-alerts.resolve", true);
        brandAlerts = s.getBoolean("client-brand-alerts.enabled", true);
        brandIgnore = list(s, "client-brand-alerts.ignore-alerts-list");
        brandMessage = str(s, "client-brand-alerts.message", "%prefix% &c%player% &7joined using &f%client-brand%&7!");
        brandBlocked = list(s, "client-brand-alerts.blocked-client-brands");
        brandKickMessage = str(s, "client-brand-alerts.unallowed-client-brand-kick-message", "&cYou can't join with %client-brand%!");
        brandConsole = s.getBoolean("client-brand-alerts.console", false);
        brandConsoleMessage = str(s, "client-brand-alerts.console-message", "[CinderAC] %player% joined using %client-brand%!");
        brandWhitelist = s.getBoolean("client-brand-alerts.whitelist-enabled", false);
        brandWhitelisted = list(s, "client-brand-alerts.whitelisted-client-brands");

        String timeout = "Internal Exception: io.netty.handler.timeout.ReadTimeoutException";
        otherAnticheatSupport = s.getBoolean("connection.other-anticheat-support", false);
        invalidKickMessage = str(s, "connection.invalid.kick-message", timeout);
        invalidStaffMessage = str(s, "connection.invalid.staff-alert-message", "");
        invalidConsoleMessage = str(s, "connection.invalid.console-alert-message", "");
        outOfOrderKickMessage = str(s, "connection.out-of-order.kick-message", timeout);
        outOfOrderStaffMessage = str(s, "connection.out-of-order.staff-alert-message", "");
        outOfOrderConsoleMessage = str(s, "connection.out-of-order.console-alert-message", "");
        noResponseDelaySeconds = s.getInt("connection.no-response.delay-in-seconds", 30);
        noResponseKickMessage = str(s, "connection.no-response.kick-message", timeout);
        noResponseStaffMessage = str(s, "connection.no-response.staff-alert-message", "");
        noResponseConsoleMessage = str(s, "connection.no-response.console-alert-message", "");
        codeOfConductFix = s.getBoolean("connection.no-response.code-of-conduct-fix", true);
        maxPendingTransactions = s.getInt("connection.max-size.amount", 2000);
        maxSizeKickMessage = str(s, "connection.max-size.kick-message", timeout);
        maxSizeStaffMessage = str(s, "connection.max-size.staff-alert-message", "");
        maxSizeConsoleMessage = str(s, "connection.max-size.console-alert-message", "");
        keepAliveKick = s.getBoolean("connection.keepalive.kick-enabled", true);
        keepAliveMaxDelay = s.getLong("connection.keepalive.max-delay", 45000L);
        keepAliveKickMessage = str(s, "connection.keepalive.kick-message", timeout);
        keepAliveStaffMessage = str(s, "connection.keepalive.staff-alert-message", "");
        keepAliveConsoleMessage = str(s, "connection.keepalive.console-message", "");
        maxPingKick = s.getBoolean("connection.max-ping.kick-enabled", true);
        maxPing = s.getInt("connection.max-ping.max-ping", 20000);
        maxPingTicks = s.getInt("connection.max-ping.max-ticks", 20);
        maxPingKickMessage = str(s, "connection.max-ping.kick-message", timeout);
        maxPingStaffMessage = str(s, "connection.max-ping.staff-alert-message", "");
        maxPingConsoleMessage = str(s, "connection.max-ping.console-message", "");

        cancelHitsAll = s.getBoolean("cancel-hits.enabled-for-all-checks", false);
        cancelHitsDuration = s.getLong("cancel-hits.duration", 3000L);
        cancelHitsEvent = s.getBoolean("cancel-hits.cancel-event", false);
        List<String> ch = new ArrayList<>();
        for (String x : list(s, "cancel-hits.checks")) ch.add(x.toLowerCase().replace(" ", ""));
        cancelHitsChecks = Collections.unmodifiableList(ch);

        punishMessage = str(s, "punishments.message", "%prefix% &c%player% &7was punished for &f%check% &7(&fType %type%&7) &7[&4x%vl%&7]");
        punishBroadcast = list(s, "punishments.broadcast");

        ghostBlocksEnabled = s.getBoolean("ghost-blocks-fix.enabled", true);
        ghostWaterFix = s.getBoolean("ghost-blocks-fix.ghost-water-fix", true);
        ghostBlocksMinTps = s.getDouble("ghost-blocks-fix.minimum-tps", 18.5);
        ghostBlocksIgnoreLegacyVia = s.getBoolean("ghost-blocks-fix.ignore-legacy-via", true);
        ghostBlocksBufferMax = s.getDouble("ghost-blocks-fix.buffer.max", 3);
        ghostBlocksBufferDecay = s.getDouble("ghost-blocks-fix.buffer.decay", 0.1);
        ghostBlocksSetback = s.getBoolean("ghost-blocks-fix.setback", true);
        ghostBlocksTicks = s.getInt("ghost-blocks-fix.ticks", 3);
        ghostBlocksMessageEnabled = s.getBoolean("ghost-blocks-fix.message-enabled", false);
        ghostBlocksMessage = str(s, "ghost-blocks-fix.message", "&cYou were walking on a ghost block. It has been updated!");
        ghostBlocksStaffMessageEnabled = s.getBoolean("ghost-blocks-fix.staff-message-enabled", true);
        ghostBlocksStaffMessage = str(s, "ghost-blocks-fix.staff-message", "%prefix% &c%player%'s &7position was updated for being on a ghost block! (%world%, %x%, %y%, %z%) &7[%ticks%]");
        ghostBlocksConsole = s.getBoolean("ghost-blocks-fix.print-to-console", true);

        toggleAlertsOnJoin = s.getBoolean("settings.toggle-alerts-on-join", true);
        perCheckSeverities = s.getBoolean("settings.per-check-severities", true);
        cinematic = s.getBoolean("settings.cinematic", false);
        punishmentDelay = s.getLong("settings.punishment-delay", 5000L);
        joinCheckWaitTime = s.getLong("settings.join-check-wait-time", 1250L);
        ignoreGeyserUuids = s.getBoolean("settings.ignore-geyser-uuids", true);
        ignoreGeyserPrefixes = s.getBoolean("settings.ignore-geyser-prefixes", true);
        geyserPrefix = str(s, "settings.ignore-geyser-prefix", "*");
        flightCooldown = s.getInt("settings.flight-cooldown", 40);
        lenientScaffolding = s.getBoolean("settings.lenient-scaffolding", true);
        entityCollision = s.getBoolean("settings.entity-collision", true);
        ignoreAlertsOverMax = s.getBoolean("settings.ignore-alerts-over-max-violations", false);
        ignoreAlertsOverMaxAmount = s.getInt("settings.ignore-alerts-over-max-violations-amount", 3);
        dontSetbackLowerPositions = s.getBoolean("settings.dont-setback-lower-positions", false);
        minTicksExisted = s.getInt("settings.min-ticks-existed", 3);
        velocityWorldBorder = s.getBoolean("settings.velocity-world-border", false);
        entityCramFix = s.getBoolean("settings.entity-cram-fix-enabled", false);
        entityCramAmount = s.getInt("settings.entity-cram-entities-amount", 10);
        entityCramExemptTicks = s.getInt("settings.entity-cram-exempt-ticks", 30);
        punishmentsEnabled = s.getBoolean("settings.punishments-enabled", false);

        msgNoPermission = str(s, "messages.no-permission", "&cYou don't have permission to execute this command!");
        msgInvalidTarget = str(s, "messages.invalid-target", "%prefix% &cThe specified player could not be found!");
        msgReload = str(s, "messages.reload-success", "%prefix% &aSuccessfully reloaded CinderAC!");
        msgResetAll = str(s, "messages.reset-command", "%prefix% &7Violations for all online players were reset!");
        msgResetPlayer = str(s, "messages.violations-reset", "%prefix% &aSuccessfully reset violations for %player%!");
        msgUnknown = str(s, "messages.unknown-command", "%prefix% &cUnknown command!");
    }

    private static String str(ConfigurationSection s, String path, String def) {
        return s.getString(path, def);
    }

    private static List<String> list(ConfigurationSection s, String path) {
        return Collections.unmodifiableList(new ArrayList<>(s.getStringList(path)));
    }

    public static Settings get() {
        return current;
    }

    public static void load(FileConfiguration c) {
        current = new Settings(c);
    }

    /** Replaces %prefix% in the text. */
    public String withPrefix(String s) {
        return s.replace("%prefix%", prefix);
    }
}
