package pl.airr.cinderac.check.player.fastbreak;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.airr.cinderac.platform.ServerVersion;

/** Expected block break time (ms) from hardness, tool, Efficiency, Haste and Mining Fatigue. */
final class BreakTime {
    private BreakTime() {
    }

    static long millis(Block block, Player player) {
        float hardness = hardness(block);
        if (hardness < 0.0f) return 0L;
        ItemStack tool = player.getItemInHand();
        String t = tool.getType().name();
        if (t.equalsIgnoreCase("SHEARS") || t.contains("COPPER")) return 0L;
        boolean drops = !block.getDrops(tool).isEmpty();
        double time = hardness * (drops ? 1.5 : 5.0);
        double speed = toolSpeed(t);
        Enchantment eff = efficiency();
        if (eff != null && tool.containsEnchantment(eff)) {
            int level = tool.getEnchantmentLevel(eff);
            speed += level * level + 1;
        }
        PotionEffectType haste = type("FAST_DIGGING", "HASTE");
        if (haste != null && player.hasPotionEffect(haste)) speed *= 1.0 + 0.2 * level(player, haste);
        PotionEffectType fatigue = type("SLOW_DIGGING", "MINING_FATIGUE");
        // XOR instead of a power - this is how the original does it
        if (fatigue != null && player.hasPotionEffect(fatigue)) speed /= 3 ^ level(player, fatigue);
        return (long) (time / speed * 1000.0);
    }

    static boolean hasHaste(Player p) {
        PotionEffectType haste = type("FAST_DIGGING", "HASTE");
        return haste != null && p.hasPotionEffect(haste);
    }

    /** shortcut: hardness from the API (1.13+); on older servers -1 = the check is inactive. */
    private static float hardness(Block block) {
        if (!ServerVersion.atLeast(13)) return -1.0f;
        try {
            return block.getType().getHardness();
        } catch (NoSuchMethodError e) {
            return -1.0f;
        }
    }

    private static int toolSpeed(String name) {
        if (name.startsWith("NETHERITE_")) return tier(name, 9);
        if (name.startsWith("DIAMOND_")) return tier(name, 8);
        if (name.startsWith("GOLDEN_") || name.startsWith("GOLD_")) return tier(name, 12);
        if (name.startsWith("IRON_")) return tier(name, 6);
        if (name.startsWith("STONE_")) return tier(name, 4);
        if (name.startsWith("WOODEN_") || name.startsWith("WOOD_")) return tier(name, 2);
        return 1;
    }

    private static int tier(String name, int speed) {
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_SPADE") ? speed : 1;
    }

    private static Enchantment efficiency() {
        Enchantment e = Enchantment.getByName("DIG_SPEED");
        return e != null ? e : Enchantment.getByName("EFFICIENCY");
    }

    private static PotionEffectType type(String legacy, String modern) {
        PotionEffectType t = PotionEffectType.getByName(legacy);
        return t != null ? t : PotionEffectType.getByName(modern);
    }

    private static int level(Player p, PotionEffectType type) {
        for (PotionEffect e : p.getActivePotionEffects()) if (e.getType().equals(type)) return e.getAmplifier() + 1;
        return 0;
    }
}
