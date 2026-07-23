package net.skyfull.tools.listener;

import net.skyfull.tools.SkyfullTools;
import net.skyfull.tools.enchant.CustomEnchant;
import net.skyfull.tools.enchant.EnchantUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Drives the block-breaking enchants: Vein Miner, Timber, Auto Smelt,
 * Excavator and Replanter. Extra blocks are removed with
 * {@link Block#breakNaturally(ItemStack)} which does NOT fire another
 * BlockBreakEvent, so there is no recursion.
 */
public class MiningListener implements Listener {

    private final SkyfullTools plugin;

    private static final Set<Material> ORES = EnumSetOf(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS);

    private static final Map<Material, Material> SMELT = smeltMap();

    private static final Set<Material> SOFT = EnumSetOf(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT,
            Material.PODZOL, Material.ROOTED_DIRT, Material.MYCELIUM,
            Material.SAND, Material.RED_SAND, Material.GRAVEL,
            Material.CLAY, Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.MUD, Material.SNOW_BLOCK);

    private static final Set<Material> CROPS = EnumSetOf(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART);

    public MiningListener(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().name().equals("CREATIVE")) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) {
            return;
        }
        Block block = event.getBlock();
        Material mat = block.getType();

        boolean autoSmelt = cfg("auto-smelt")
                && isPickaxe(tool) && EnchantUtil.has(tool, CustomEnchant.AUTO_SMELT)
                && SMELT.containsKey(mat);

        // ---- Auto Smelt on the original block ----
        if (autoSmelt) {
            event.setDropItems(false);
            dropSmelted(block, mat);
            int bonus = plugin.getConfig().getInt("enchants.auto-smelt.bonus-xp", 1);
            event.setExpToDrop(event.getExpToDrop() + bonus);
        }

        // ---- Vein Miner ----
        if (cfg("vein-miner") && isPickaxe(tool)
                && EnchantUtil.has(tool, CustomEnchant.VEIN_MINER) && ORES.contains(mat)) {
            int level = EnchantUtil.getLevel(tool, CustomEnchant.VEIN_MINER);
            int perLevel = plugin.getConfig().getInt("enchants.vein-miner.blocks-per-level", 32);
            floodBreak(player, tool, block, mat, level * perLevel, autoSmelt, true);
        }
        // ---- Timber ----
        else if (cfg("timber") && isAxe(tool)
                && EnchantUtil.has(tool, CustomEnchant.TIMBER) && Tag.LOGS.isTagged(mat)) {
            int max = plugin.getConfig().getInt("enchants.timber.max-logs", 150);
            floodBreak(player, tool, block, mat, max, false, false);
        }
        // ---- Excavator ----
        else if (cfg("excavator") && isShovel(tool)
                && EnchantUtil.has(tool, CustomEnchant.EXCAVATOR) && SOFT.contains(mat)) {
            excavate(player, tool, block);
        }

        // ---- Replanter (independent of the above) ----
        if (cfg("replanter") && isHoe(tool)
                && EnchantUtil.has(tool, CustomEnchant.REPLANTER) && CROPS.contains(mat)) {
            if (isMature(block)) {
                scheduleReplant(block, mat);
            }
        }
    }

    /* ----------------------------------------------------------------- */

    /** Breadth-first break of connected blocks of {@code match} material. */
    private void floodBreak(Player player, ItemStack tool, Block start,
                            Material match, int maxBlocks, boolean smelt, boolean sameMaterial) {
        Set<Block> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        int broken = 0;

        while (!queue.isEmpty() && broken < maxBlocks) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        Block next = current.getRelative(dx, dy, dz);
                        if (visited.contains(next)) {
                            continue;
                        }
                        boolean matches = sameMaterial
                                ? next.getType() == match
                                : Tag.LOGS.isTagged(next.getType());
                        if (!matches) {
                            continue;
                        }
                        visited.add(next);
                        if (broken >= maxBlocks) {
                            continue;
                        }
                        if (breakExtra(player, tool, next, smelt)) {
                            broken++;
                            queue.add(next);
                        }
                        if (isToolAboutToBreak(tool)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /** Breaks a single extra block and applies tool damage. */
    private boolean breakExtra(Player player, ItemStack tool, Block block, boolean smelt) {
        Material mat = block.getType();
        if (smelt && SMELT.containsKey(mat)) {
            dropSmelted(block, mat);
            block.setType(Material.AIR);
        } else {
            block.breakNaturally(tool);
        }
        damageTool(tool, 1);
        return true;
    }

    private void dropSmelted(Block block, Material ore) {
        Material result = SMELT.get(ore);
        if (result == null) {
            return;
        }
        World world = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        world.dropItemNaturally(loc, new ItemStack(result));
    }

    private void excavate(Player player, ItemStack tool, Block center) {
        Vector dir = player.getEyeLocation().getDirection();
        double ax = Math.abs(dir.getX());
        double ay = Math.abs(dir.getY());
        double az = Math.abs(dir.getZ());

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue; // centre handled by the event itself
                }
                Block target;
                if (ay >= ax && ay >= az) {
                    target = center.getRelative(a, 0, b);   // looking up/down -> flat 3x3
                } else if (ax >= az) {
                    target = center.getRelative(0, a, b);   // looking along X -> vertical slice
                } else {
                    target = center.getRelative(a, b, 0);   // looking along Z -> vertical slice
                }
                if (SOFT.contains(target.getType())) {
                    target.breakNaturally(tool);
                    damageTool(tool, 1);
                    if (isToolAboutToBreak(tool)) {
                        return;
                    }
                }
            }
        }
    }

    private void scheduleReplant(Block block, Material crop) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (block.getType() == Material.AIR) {
                block.setType(crop);
                if (block.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(0);
                    block.setBlockData(ageable);
                }
            }
        });
    }

    /* ----------------------------------------------------------------- */

    private boolean isMature(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            return ageable.getAge() >= ageable.getMaximumAge();
        }
        return false;
    }

    private void damageTool(ItemStack tool, int amount) {
        if (tool.getType().getMaxDurability() <= 0) {
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable dmg)) {
            return;
        }
        dmg.setDamage(dmg.getDamage() + amount);
        tool.setItemMeta(meta);
    }

    private boolean isToolAboutToBreak(ItemStack tool) {
        int max = tool.getType().getMaxDurability();
        if (max <= 0) {
            return false;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable dmg) {
            return dmg.getDamage() >= max - 1;
        }
        return false;
    }

    private boolean cfg(String enchant) {
        return plugin.getConfig().getBoolean("enchants." + enchant + ".enabled", true);
    }

    private static boolean isPickaxe(ItemStack i) {
        return i.getType().name().endsWith("_PICKAXE");
    }

    private static boolean isAxe(ItemStack i) {
        return i.getType().name().endsWith("_AXE");
    }

    private static boolean isShovel(ItemStack i) {
        return i.getType().name().endsWith("_SHOVEL");
    }

    private static boolean isHoe(ItemStack i) {
        return i.getType().name().endsWith("_HOE");
    }

    private static Set<Material> EnumSetOf(Material... mats) {
        Set<Material> set = new HashSet<>();
        for (Material m : mats) {
            set.add(m);
        }
        return set;
    }

    private static Map<Material, Material> smeltMap() {
        Map<Material, Material> m = new EnumMap<>(Material.class);
        m.put(Material.IRON_ORE, Material.IRON_INGOT);
        m.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        m.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        m.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        m.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        m.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        m.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        m.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        return m;
    }
}
