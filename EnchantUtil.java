package net.skyfull.tools.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyfull.tools.Keys;
import net.skyfull.tools.SkyfullTools;
import net.skyfull.tools.item.CustomItem;
import net.skyfull.tools.item.ItemFactory;
import net.skyfull.tools.item.MagnetItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Handles right-click behaviour for every custom gadget item. */
public class ItemUseListener implements Listener {

    private final SkyfullTools plugin;
    private final Map<UUID, Long> warpCooldown = new java.util.HashMap<>();

    public ItemUseListener(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // only process the main hand once
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        CustomItem type = ItemFactory.typeOf(item);
        if (type == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        switch (type) {
            case ENDER_POUCH -> useEnderPouch(player);
            case MAGNET -> toggleMagnet(player, item);
            case GREEN_THUMB_WAND -> useWand(player, event.getClickedBlock());
            case WARP_STONE -> useWarpStone(player);
            case PROSPECTORS_COMPASS -> useCompass(player);
        }
    }

    /* ------------------------------------------------------------- */

    private void useEnderPouch(Player player) {
        player.openInventory(player.getEnderChest());
        player.playSound(player.getLocation(), "minecraft:block.ender_chest.open", 0.7f, 1.2f);
    }

    private void toggleMagnet(Player player, ItemStack item) {
        if (!player.isSneaking()) {
            msg(player, NamedTextColor.GRAY, "Shift + right-click to toggle the magnet.");
            return;
        }
        boolean now = !MagnetItem.isEnabled(item);
        MagnetItem.setEnabled(item, now);
        if (now) {
            msg(player, NamedTextColor.GREEN, "Magnet switched ON.");
            player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 0.6f, 2f);
        } else {
            msg(player, NamedTextColor.RED, "Magnet switched OFF.");
            player.playSound(player.getLocation(), "minecraft:block.note_block.bass", 0.6f, 1f);
        }
    }

    private void useWand(Player player, Block clicked) {
        int r = plugin.getConfig().getInt("items.green-thumb-wand.radius", 2);
        int cost = plugin.getConfig().getInt("items.green-thumb-wand.cost-per-plant", 1);
        Block base = clicked != null ? clicked : player.getLocation().getBlock();

        int grown = 0;
        outer:
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (countBoneMeal(player) < cost) {
                        break outer;
                    }
                    Block b = base.getRelative(dx, dy, dz);
                    try {
                        if (b.applyBoneMeal(BlockFace.UP)) {
                            removeBoneMeal(player, cost);
                            grown++;
                            b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                    b.getLocation().add(0.5, 0.5, 0.5), 6, 0.3, 0.3, 0.3);
                        }
                    } catch (Throwable ignored) {
                        // block type does not accept bone meal
                    }
                }
            }
        }
        if (grown > 0) {
            msg(player, NamedTextColor.GREEN, "The wand nurtured " + grown + " plant(s).");
            player.playSound(player.getLocation(), "minecraft:item.bone_meal.use", 0.8f, 1.2f);
        } else {
            msg(player, NamedTextColor.GRAY, "Nothing to grow here (need Bone Meal + crops).");
        }
    }

    private void useWarpStone(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (player.isSneaking()) {
            Location l = player.getLocation();
            pdc.set(Keys.HOME_WORLD, PersistentDataType.STRING, l.getWorld().getName());
            pdc.set(Keys.HOME_X, PersistentDataType.DOUBLE, l.getX());
            pdc.set(Keys.HOME_Y, PersistentDataType.DOUBLE, l.getY());
            pdc.set(Keys.HOME_Z, PersistentDataType.DOUBLE, l.getZ());
            msg(player, NamedTextColor.AQUA, "Home set at your current location.");
            player.playSound(player.getLocation(), "minecraft:block.beacon.activate", 0.7f, 1.5f);
            return;
        }

        String worldName = pdc.get(Keys.HOME_WORLD, PersistentDataType.STRING);
        if (worldName == null) {
            msg(player, NamedTextColor.RED, "No home set. Shift + right-click to set one first.");
            return;
        }
        long now = System.currentTimeMillis();
        long cdMs = plugin.getConfig().getInt("items.warp-stone.cooldown-seconds", 30) * 1000L;
        Long last = warpCooldown.get(player.getUniqueId());
        if (last != null && now - last < cdMs) {
            long left = (cdMs - (now - last)) / 1000L + 1;
            msg(player, NamedTextColor.RED, "Warp Stone on cooldown (" + left + "s).");
            return;
        }

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            msg(player, NamedTextColor.RED, "Your home world is not loaded.");
            return;
        }
        Location home = new Location(world,
                pdc.get(Keys.HOME_X, PersistentDataType.DOUBLE),
                pdc.get(Keys.HOME_Y, PersistentDataType.DOUBLE),
                pdc.get(Keys.HOME_Z, PersistentDataType.DOUBLE));

        int warmup = plugin.getConfig().getInt("items.warp-stone.warmup-seconds", 3);
        warpCooldown.put(player.getUniqueId(), now);
        msg(player, NamedTextColor.AQUA, "Warping home in " + warmup + "s - don't move!");
        final Location start = player.getLocation();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (player.getLocation().distanceSquared(start) > 1.0) {
                msg(player, NamedTextColor.RED, "Warp cancelled - you moved.");
                warpCooldown.remove(player.getUniqueId());
                return;
            }
            player.teleport(home);
            player.playSound(home, "minecraft:entity.enderman.teleport", 1f, 1f);
            msg(player, NamedTextColor.AQUA, "Welcome home.");
        }, warmup * 20L);
    }

    private void useCompass(Player player) {
        int r = plugin.getConfig().getInt("items.prospectors-compass.scan-radius", 10);
        int highlightSecs = plugin.getConfig().getInt("items.prospectors-compass.highlight-seconds", 6);
        Location center = player.getLocation();
        World world = center.getWorld();

        Map<Material, Integer> counts = new LinkedHashMap<>();
        List<Location> spots = new ArrayList<>();
        int bx = center.getBlockX();
        int by = center.getBlockY();
        int bz = center.getBlockZ();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Block b = world.getBlockAt(bx + x, by + y, bz + z);
                    Material m = b.getType();
                    if (m.name().endsWith("_ORE") || m == Material.ANCIENT_DEBRIS) {
                        counts.merge(m, 1, Integer::sum);
                        if (spots.size() < 300) {
                            spots.add(b.getLocation().add(0.5, 0.5, 0.5));
                        }
                    }
                }
            }
        }

        if (counts.isEmpty()) {
            msg(player, NamedTextColor.GRAY, "No ores within " + r + " blocks.");
            player.playSound(center, "minecraft:block.note_block.bass", 0.6f, 0.7f);
            return;
        }
        msg(player, NamedTextColor.GOLD, "Ores nearby (" + r + " blocks):");
        for (Map.Entry<Material, Integer> e : counts.entrySet()) {
            String name = prettyName(e.getKey());
            player.sendMessage(Component.text("  " + name + ": " + e.getValue(), NamedTextColor.YELLOW));
        }
        player.playSound(center, "minecraft:block.amethyst_block.chime", 0.8f, 1.4f);
        highlight(spots, highlightSecs);
    }

    private void highlight(List<Location> spots, int seconds) {
        final int iterations = Math.max(1, seconds * 2); // twice per second
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= iterations || spots.isEmpty()) {
                    cancel();
                    return;
                }
                for (Location l : spots) {
                    l.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, l, 4, 0.25, 0.25, 0.25, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /* ------------------------------------------------------------- */

    private int countBoneMeal(Player player) {
        int total = 0;
        for (ItemStack it : player.getInventory().getContents()) {
            if (it != null && it.getType() == Material.BONE_MEAL) {
                total += it.getAmount();
            }
        }
        return total;
    }

    private void removeBoneMeal(Player player, int amount) {
        player.getInventory().removeItem(new ItemStack(Material.BONE_MEAL, amount));
    }

    private String prettyName(Material m) {
        String s = m.name().toLowerCase().replace('_', ' ');
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void msg(Player player, NamedTextColor color, String text) {
        player.sendMessage(Component.text("[SKYFULL] ", NamedTextColor.DARK_AQUA)
                .append(Component.text(text, color)));
    }
}
