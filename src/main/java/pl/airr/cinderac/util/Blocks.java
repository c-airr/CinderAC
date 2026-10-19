package pl.airr.cinderac.util;

import org.bukkit.Material;

import java.util.function.Predicate;

/**
 * Block groups recognised by material name (works with both 1.8 and 1.13+ names).
 * Computed once per Material, then read from an array (safe from multiple threads).
 */
public enum Blocks {
    AIR(n -> n.equals("AIR") || n.equals("CAVE_AIR") || n.equals("VOID_AIR")),
    LIQUID(n -> n.contains("LAVA") || n.contains("WATER") || n.contains("BUBBLE")),
    /** Water/lava only (1.8: also STATIONARY_*). */
    LIQUID_BLOCK(n -> n.equals("WATER") || n.equals("LAVA") || n.equals("STATIONARY_WATER") || n.equals("STATIONARY_LAVA")),
    LAVA(n -> n.contains("LAVA")),
    WATER(n -> n.contains("WATER")),
    ICE(n -> n.equals("ICE") || n.equals("PACKED_ICE") || n.equals("BLUE_ICE") || n.equals("FROSTED_ICE")),
    BLUE_ICE(n -> n.equals("BLUE_ICE")),
    FROSTED_ICE(n -> n.equals("FROSTED_ICE")),
    SLIME_BLOCK(n -> n.equals("SLIME_BLOCK")),
    SLIME(n -> n.contains("SLIME")),
    CLIMBABLE(n -> n.equals("LADDER") || n.contains("VINE") || n.equals("SCAFFOLDING")),
    CLIMBABLE_LOOSE(n -> n.contains("LADDER") || n.contains("VINE") || n.equalsIgnoreCase("SCAFFOLDING")),
    WEB(n -> n.equals("WEB") || n.equals("COBWEB")),
    WEB_LOOSE(n -> n.contains("WEB")),
    STAIRS(n -> n.contains("STAIR")),
    SLAB(n -> n.contains("SLAB") || n.contains("STEP")),
    FENCE(n -> n.contains("FENCE")),
    GATE(n -> n.contains("GATE")),
    /** Walls etc. - name contains WALL but not SIGN. */
    WALL(n -> n.contains("WALL") && !n.contains("SIGN")),
    PANE(n -> n.contains("PANE")),
    GLASS(n -> n.contains("GLASS") && !n.contains("PANE")),
    BED(n -> n.contains("BED") && !n.equals("BEDROCK")),
    TRAPDOOR(n -> n.contains("TRAP") && n.contains("DOOR")),
    DOOR(n -> n.contains("DOOR")),
    SNOW(n -> n.equals("SNOW")),
    SOUL_SAND(n -> n.equalsIgnoreCase("SOUL_SAND")),
    SOUL_SAND_SOIL(n -> n.equals("SOUL_SAND") || n.equalsIgnoreCase("SOUL_SOIL")),
    SOUL(n -> n.contains("SOUL")),
    HONEY_BLOCK(n -> n.equals("HONEY_BLOCK")),
    HONEY(n -> n.contains("HONEY")),
    CARPET(n -> n.contains("CARPET")),
    LILY_PAD(n -> n.equals("LILY_PAD") || n.equals("WATER_LILY")),
    LILY(n -> n.contains("LILY")),
    PISTON(n -> n.contains("PISTON")),
    SCAFFOLDING(n -> n.equals("SCAFFOLDING")),
    POWDER_SNOW(n -> n.equals("POWDER_SNOW")),
    BUBBLE_COLUMN(n -> n.equals("BUBBLE_COLUMN")),
    BUBBLE(n -> n.contains("BUBBLE")),
    SHULKER(n -> n.contains("SHULKER")),
    KELP(n -> n.contains("KELP")),
    SEAGRASS(n -> n.contains("GRASS") && n.contains("SEA")),
    CHEST(n -> n.contains("CHEST")),
    SKULL(n -> n.contains("SKULL") || n.contains("HEAD")),
    CAKE(n -> n.contains("CAKE")),
    BAMBOO(n -> n.contains("BAMBOO")),
    SIGN(n -> n.contains("SIGN")),
    PLATE(n -> n.contains("PLATE")),
    DRIPSTONE(n -> n.contains("DRIPSTONE")),
    CAMPFIRE(n -> n.equals("CAMPFIRE") || n.equals("SOUL_CAMPFIRE")),
    CAMPFIRE_LOOSE(n -> n.contains("CAMPFIRE")),
    FLOWER_POT(n -> n.equals("FLOWER_POT")),
    END_ROD(n -> n.equals("END_ROD")),
    HOPPER(n -> n.equals("HOPPER")),
    CHAIN(n -> n.equals("CHAIN")),
    CHAIN_LOOSE(n -> n.contains("CHAIN")),
    ANVIL(n -> n.equals("ANVIL") || n.equals("DAMAGED_ANVIL") || n.equals("CHIPPED_ANVIL")),
    ANVIL_LOOSE(n -> n.contains("ANVIL")),
    CONDUIT(n -> n.equals("CONDUIT")),
    SEA_PICKLE(n -> n.equals("SEA_PICKLE")),
    PICKLE(n -> n.contains("PICKLE")),
    TURTLE_EGG(n -> n.equals("TURTLE_EGG")),
    TURTLE_EGG_LOOSE(n -> n.contains("TURTLE") && n.contains("EGG")),
    CAULDRON(n -> n.equals("CAULDRON")),
    IRON_BARS(n -> n.equals("IRON_BARS")),
    ENCHANT(n -> n.contains("ENCHANT")),
    AMETHYST(n -> n.contains("AMETHYST")),
    RAIL(n -> n.contains("RAIL")),
    LANTERN(n -> n.contains("LANTERN")),
    BELL(n -> n.equals("BELL")),
    BELL_LOOSE(n -> n.contains("BELL")),
    LECTERN(n -> n.equals("LECTERN")),
    LECTERN_LOOSE(n -> n.contains("LECTERN")),
    BERRY_BUSH(n -> n.equals("SWEET_BERRY_BUSH")),
    SWEET(n -> n.contains("SWEET")),
    FARMLAND(n -> n.contains("FARMLAND")),
    PATH(n -> n.contains("PATH")),
    SCULK(n -> n.contains("SCULK")),
    SCULK_SENSOR(n -> n.equals("SCULK_SENSOR")),
    CANDLE(n -> n.contains("CANDLE")),
    DRIPLEAF(n -> n.contains("DRIP") && n.contains("LEAF")),
    CACTUS(n -> n.equalsIgnoreCase("CACTUS")),
    DAYLIGHT_DETECTOR(n -> n.equals("DAYLIGHT_DETECTOR")),
    DAYLIGHT(n -> n.contains("DAYLIGHT")),
    BREWING_STAND(n -> n.equals("BREWING_STAND")),
    BREWING(n -> n.contains("BREWING")),
    REPEATER(n -> n.equals("REPEATER") || n.equals("DIODE")),
    END_PORTAL_FRAME(n -> n.equals("END_PORTAL_FRAME") || n.equals("ENDER_PORTAL_FRAME")),
    FRAME(n -> n.contains("FRAME")),
    HEAVY_CORE(n -> n.equals("HEAVY_CORE")),
    DRIED_GHAST(n -> n.equals("DRIED_GHAST")),
    NOTE_BLOCK(n -> n.equals("NOTE_BLOCK")),
    WIRE(n -> n.contains("WIRE") || n.contains("STRING")),
    BOAT(n -> n.contains("BOAT")),
    TNT(n -> n.equals("TNT")),
    DEEPSLATE(n -> n.contains("DEEPSLATE")),
    /** Sand (name contains SAND but not STONE). */
    SAND(n -> n.contains("SAND") && !n.contains("STONE")),
    GRAVEL(n -> n.contains("GRAVEL")),
    CONCRETE_POWDER(n -> n.contains("CONCRETE") && n.contains("POWDER")),
    STRIPPED(n -> n.contains("STRIPPED")),
    ANCHOR(n -> n.contains("ANCHOR")),
    GLASS_BOTTLE(n -> n.equals("GLASS_BOTTLE")),
    GROWABLE(n -> n.contains("GROWABLE")),
    FIREWORK(n -> n.contains("FIREWORK"));

    private final boolean[] table;

    Blocks(Predicate<String> test) {
        Material[] all = Material.values();
        table = new boolean[all.length];
        for (Material m : all) table[m.ordinal()] = test.test(m.name());
    }

    public boolean is(Material m) {
        return m != null && table[m.ordinal()];
    }

    public static boolean isAir(Material m) {
        return AIR.is(m);
    }

    public static boolean isSolid(Material m) {
        return m != null && m.isSolid();
    }

    public static boolean any(Iterable<Material> list, Blocks b) {
        if (list == null) return false;
        for (Material m : list) if (b.is(m)) return true;
        return false;
    }

    /** All materials in the list belong to the group (empty list = true, like allMatch). */
    public static boolean all(Iterable<Material> list, Blocks b) {
        if (list == null) return false;
        for (Material m : list) if (!b.is(m)) return false;
        return true;
    }

    public static boolean anySolid(Iterable<Material> list) {
        if (list == null) return false;
        for (Material m : list) if (isSolid(m)) return true;
        return false;
    }
}
