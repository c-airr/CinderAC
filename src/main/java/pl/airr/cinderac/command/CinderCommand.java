package pl.airr.cinderac.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckRegistry;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /cinderac alerts|reload|reset|violations|profile|disablecheck|clickalert */
public final class CinderCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBS = Arrays.asList("help", "alerts", "reload", "reset", "violations", "profile", "disablecheck");
    private final CinderAC plugin;

    public CinderCommand(CinderAC plugin) {
        this.plugin = plugin;
    }

    private static void msg(CommandSender s, String text) {
        s.sendMessage(Text.color(Settings.get().withPrefix(text)));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Settings s = Settings.get();
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        if (sub.equals("alerts")) {
            if (!(sender instanceof Player) || !sender.hasPermission("cinderac.alerts")) {
                msg(sender, s.msgNoPermission);
                return true;
            }
            msg(sender, plugin.alerts().toggle((Player) sender) ? s.toggledOn : s.toggledOff);
            return true;
        }
        if (!sender.hasPermission("cinderac.command")) {
            msg(sender, s.msgNoPermission);
            return true;
        }
        switch (sub) {
            case "reload":
                plugin.loadConfiguration();
                msg(sender, s.msgReload);
                return true;
            case "reset": {
                if (args.length < 2) {
                    plugin.resetViolations();
                    msg(sender, s.msgResetAll);
                    return true;
                }
                PlayerData d = target(sender, args[1]);
                if (d == null) return true;
                plugin.resetViolations(d);
                msg(sender, s.msgResetPlayer.replace("%player%", d.name));
                return true;
            }
            case "violations": {
                if (args.length < 2) {
                    msg(sender, "%prefix% &c/cinderac violations <player>");
                    return true;
                }
                PlayerData d = target(sender, args[1]);
                if (d == null) return true;
                List<String> lines = new ArrayList<>();
                synchronized (d) {
                    for (Check c : d.checks) if (c.vl() > 0) lines.add("&7" + c.info.name() + " " + c.info.type() + ": &c" + c.vl());
                }
                msg(sender, "%prefix% &7Violations of &c" + d.name + "&7 (" + d.totalViolations + "):");
                if (lines.isEmpty()) msg(sender, "&7none");
                for (String l : lines) msg(sender, l);
                return true;
            }
            case "profile": {
                if (args.length < 2) {
                    msg(sender, "%prefix% &c/cinderac profile <player>");
                    return true;
                }
                PlayerData d = target(sender, args[1]);
                if (d == null) return true;
                DecimalFormat f = new DecimalFormat("#.##");
                msg(sender, "&7&m---------------------------------");
                msg(sender, "&cCinderAC: &f" + d.name + " &8(" + d.uuid + ")");
                msg(sender, "&7Version: &c" + d.clientVersion().getReleaseName() + " &7Client: &c" + d.clientBrand);
                msg(sender, "&7Sensitivity: &c" + d.rotation.sensitivity + "% &7CPS: &c" + f.format(d.clicks.cps)
                        + " &7Speed: &c" + f.format(d.action.movementSpeedAttribute));
                msg(sender, "&7Ping (transactions/keepalive): &c" + d.transactions.transactionPing + "ms / " + d.keepAlive.ping + "ms");
                msg(sender, "&7Violations: &c" + d.totalViolations + " &8(combat " + d.combatViolations + ", movement "
                        + d.movementViolations + ", player " + d.playerViolations + ")");
                msg(sender, "&7&m---------------------------------");
                return true;
            }
            case "disablecheck": {
                if (args.length < 2) {
                    msg(sender, "%prefix% &c/cinderac disablecheck <e.g. AimA>");
                    return true;
                }
                boolean known = false;
                for (Class<? extends Check> c : CheckRegistry.CHECKS) known |= c.getSimpleName().equals(args[1]);
                if (!known) {
                    msg(sender, "%prefix% &cUnknown check! Examples: AimA, BadPacketsF, AutoClickerJ.");
                    return true;
                }
                plugin.disableCheck(args[1]);
                msg(sender, "%prefix% &cDisabled check " + args[1] + " (until reload).");
                return true;
            }
            case "clickalert": {
                if (!(sender instanceof Player) || args.length < 2) return true;
                Player staff = (Player) sender;
                for (String c : s.clickAlertCommands) staff.performCommand(c.replace("%player%", args[1]));
                return true;
            }
            default:
                msg(sender, "&c/cinderac alerts &8- &7toggle alerts");
                msg(sender, "&c/cinderac reload &8- &7reload the config");
                msg(sender, "&c/cinderac reset [player] &8- &7reset violations");
                msg(sender, "&c/cinderac violations <player> &8- &7player violations");
                msg(sender, "&c/cinderac profile <player> &8- &7player profile");
                msg(sender, "&c/cinderac disablecheck <check> &8- &7disable a check until reload");
                return true;
        }
    }

    private PlayerData target(CommandSender sender, String name) {
        Player p = Bukkit.getPlayerExact(name);
        PlayerData d = p == null ? null : plugin.players().get(p);
        if (d == null) msg(sender, Settings.get().msgInvalidTarget);
        return d;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : SUBS) if (s.startsWith(args[0].toLowerCase())) out.add(s);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("disablecheck")) {
            for (Class<? extends Check> c : CheckRegistry.CHECKS) if (c.getSimpleName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(c.getSimpleName());
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
        }
        return out;
    }
}
