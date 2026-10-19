package pl.airr.cinderac.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** '&' colors and '&#RRGGBB' hex (hex only on 1.16+, older servers keep the text unchanged). */
public final class Text {
    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");

    private Text() {
    }

    public static String color(String s) {
        if (s == null) return "";
        Matcher m = HEX.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            StringBuilder rep = new StringBuilder("§x");
            for (char c : m.group(1).toCharArray()) rep.append('§').append(c);
            m.appendReplacement(out, Matcher.quoteReplacement(rep.toString()));
        }
        m.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }

    public static String strip(String s) {
        return ChatColor.stripColor(color(s));
    }
}
