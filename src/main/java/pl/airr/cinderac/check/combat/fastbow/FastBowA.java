package pl.airr.cinderac.check.combat.fastbow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Fast Bow", type = 'A', complexType = "Limit", description = "Shooting a bow too quickly")
public final class FastBowA extends Check {
    public FastBowA(PlayerData data) {
        super(data);
    }

    private long lastShot;

    @Override
    public void handle(Packet p) {
    }

    @Override
    public void handleBukkit(Event event) {
        if (!(event instanceof ProjectileLaunchEvent) || !(((ProjectileLaunchEvent) event).getEntity() instanceof Arrow)) return;
        Player pl = data.player;
        if (pl == null) return;
        Arrow arrow = (Arrow) ((ProjectileLaunchEvent) event).getEntity();
        boolean crossbow = holds(pl, "CROSS");
        boolean bow = holds(pl, "BOW");
        if (ServerVersion.atLeast(14) && (multishot(pl.getInventory().getItemInMainHand()) || multishot(pl.getInventory().getItemInOffHand()))) return;
        if (!(arrow.getVelocity().length() > 2.0) || crossbow || !bow) return;
        long delay = System.currentTimeMillis() - lastShot;
        if (delay < 750L && !exempt(Exempt.FAST, Exempt.ELYTRA, Exempt.GLIDING)) {
            if (increaseBuffer() > maxBuffer()) fail("length=" + arrow.getVelocity().length() + " delay=" + delay);
        } else {
            decreaseBuffer();
        }
        lastShot = System.currentTimeMillis();
    }

    private static boolean holds(Player p, String part) {
        if (p.getItemInHand().getType().name().contains(part)) return true;
        return ServerVersion.atLeast(9) && p.getInventory().getItemInOffHand().getType().name().contains(part);
    }

    private static boolean multishot(ItemStack item) {
        Enchantment e = Enchantment.getByName("MULTISHOT");
        return item != null && e != null && item.getEnchantmentLevel(e) > 0;
    }
}
