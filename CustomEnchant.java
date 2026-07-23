package net.skyfull.tools.listener;

import net.skyfull.tools.SkyfullTools;
import net.skyfull.tools.enchant.CustomEnchant;
import net.skyfull.tools.enchant.EnchantUtil;
import net.skyfull.tools.item.CustomItem;
import net.skyfull.tools.item.ItemFactory;
import net.skyfull.tools.item.MagnetItem;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Repeating task that pulls nearby dropped items to any player who is either
 * holding an enabled Magnet gadget or wearing/holding a Magnetic-enchanted item.
 */
public class MagnetTask implements Runnable {

    private final SkyfullTools plugin;

    public MagnetTask(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        boolean magnetItemOn = plugin.getConfig().getBoolean("items.magnet.enabled", true);
        boolean enchantOn = plugin.getConfig().getBoolean("enchants.magnetic.enabled", true);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isDead() || !player.isOnline()) {
                continue;
            }
            int radius = radiusFor(player, magnetItemOn, enchantOn);
            if (radius <= 0) {
                continue;
            }
            pull(player, radius);
        }
    }

    private int radiusFor(Player player, boolean magnetItemOn, boolean enchantOn) {
        int radius = 0;

        if (magnetItemOn) {
            for (ItemStack it : player.getInventory().getContents()) {
                if (ItemFactory.typeOf(it) == CustomItem.MAGNET && MagnetItem.isEnabled(it)) {
                    radius = Math.max(radius, plugin.getConfig().getInt("items.magnet.radius", 6));
                    break;
                }
            }
        }

        if (enchantOn) {
            int base = plugin.getConfig().getInt("enchants.magnetic.base-radius", 4);
            for (ItemStack it : gearOf(player)) {
                int lvl = EnchantUtil.getLevel(it, CustomEnchant.MAGNETIC);
                if (lvl > 0) {
                    radius = Math.max(radius, base + lvl);
                }
            }
        }
        return radius;
    }

    private ItemStack[] gearOf(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] all = new ItemStack[armor.length + 2];
        System.arraycopy(armor, 0, all, 0, armor.length);
        all[armor.length] = player.getInventory().getItemInMainHand();
        all[armor.length + 1] = player.getInventory().getItemInOffHand();
        return all;
    }

    private void pull(Player player, int radius) {
        boolean pickedAny = false;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Item drop)) {
                continue;
            }
            if (drop.getPickupDelay() > 40 || drop.getTicksLived() < 10) {
                continue; // respect no-pickup flags and just-thrown items
            }
            ItemStack stack = drop.getItemStack();
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack.clone());
            if (leftover.isEmpty()) {
                drop.remove();
                pickedAny = true;
            } else {
                drop.setItemStack(leftover.get(0));
            }
        }
        if (pickedAny) {
            player.playSound(player.getLocation(), "minecraft:entity.item.pickup", 0.25f, 1.8f);
        }
    }
}
