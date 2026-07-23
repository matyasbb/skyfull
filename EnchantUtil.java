package net.skyfull.tools.listener;

import net.skyfull.tools.SkyfullTools;
import net.skyfull.tools.enchant.CustomEnchant;
import net.skyfull.tools.enchant.EnchantUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps Soulbound items out of the death drops and returns them on respawn.
 */
public class SoulboundListener implements Listener {

    private final SkyfullTools plugin;
    private final Map<UUID, List<ItemStack>> stored = new ConcurrentHashMap<>();

    public SoulboundListener(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("enchants.soulbound.enabled", true)) {
            return;
        }
        if (event.getKeepInventory()) {
            return; // server already keeps everything
        }
        Player player = event.getEntity();
        List<ItemStack> keep = new ArrayList<>();

        Iterator<ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            ItemStack drop = it.next();
            if (EnchantUtil.has(drop, CustomEnchant.SOULBOUND)) {
                keep.add(drop);
                it.remove();
            }
        }
        if (!keep.isEmpty()) {
            stored.put(player.getUniqueId(), keep);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> items = stored.remove(player.getUniqueId());
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(items.toArray(new ItemStack[0]));
        for (ItemStack over : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), over);
        }
    }
}
