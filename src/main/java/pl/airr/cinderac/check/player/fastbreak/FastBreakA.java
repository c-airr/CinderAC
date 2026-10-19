package pl.airr.cinderac.check.player.fastbreak;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Fast Break", type = 'A', complexType = "Delay", description = "Breaking blocks too quickly.")
public final class FastBreakA extends Check {
    public FastBreakA(PlayerData data) {
        super(data);
    }

    private long expectedEnd;

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (data.player == null) return;
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) event;
            if (e.getAction() != Action.LEFT_CLICK_BLOCK || e.isCancelled() || e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
            Block block = e.getClickedBlock();
            if (block == null || instant(block.getType()) || copperTool(e.getPlayer())) return;
            expectedEnd = System.currentTimeMillis() + BreakTime.millis(block, e.getPlayer());
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            Block block = e.getBlock();
            if (e.isCancelled() || data.action.instaBreak || e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
            if (exempt(Exempt.VEHICLE) || instant(block.getType()) || copperTool(e.getPlayer()) || boosted()) return;
            if (ServerVersion.atLeast(16) && BreakTime.hasHaste(e.getPlayer())) return;
            double diff = System.currentTimeMillis() - expectedEnd;
            if (diff < cfg.getInt("min-difference", -60)) {
                if (increaseBuffer() > maxBuffer()) {
                    fail("block=" + block.getType() + " diff=" + diff + " tool=" + data.player.getItemInHand().getType());
                }
            } else {
                decreaseBuffer();
            }
        }
    }

    private static boolean copperTool(Player p) {
        if (p.getItemInHand().getType().name().contains("COPPER")) return true;
        return ServerVersion.atLeast(9) && p.getInventory().getItemInOffHand().getType().name().contains("COPPER");
    }

    private boolean boosted() {
        Enchantment eff = Enchantment.getByName("DIG_SPEED");
        if (eff == null) eff = Enchantment.getByName("EFFICIENCY");
        return data.action.mcmmoAbility || data.action.hasConduitPower || data.action.aquaAffinity
                || eff != null && data.player.getItemInHand().getEnchantmentLevel(eff) > 5;
    }

    /** Blocks broken instantly or with unusual timing - skipped. */
    private static boolean instant(Material m) {
        String n = m.name();
        return Blocks.LIQUID.is(m) || Blocks.DOOR.is(m) || Blocks.TRAPDOOR.is(m) || Blocks.SCULK.is(m) || Blocks.AMETHYST.is(m)
                || Blocks.SHULKER.is(m) || Blocks.DEEPSLATE.is(m) || Blocks.PISTON.is(m) || Blocks.isAir(m)
                || n.contains("DOOR") || n.contains("TRAP") || n.contains("INFESTED") || n.contains("CONCRETE") || n.contains("SCULK")
                || n.contains("MUSHROOM") || n.contains("NOTEBLOCK") || n.equals("WATER") || n.equals("WATER_LILY")
                || n.equals("MANGROVE_ROOTS") || n.equals("CHISELED_BOOKSHELF") || n.equals("FIRE");
    }
}
