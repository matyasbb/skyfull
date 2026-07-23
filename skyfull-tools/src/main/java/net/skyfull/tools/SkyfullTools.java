package net.skyfull.tools;

import net.skyfull.tools.command.SkyfullCommand;
import net.skyfull.tools.item.CustomItem;
import net.skyfull.tools.item.ItemFactory;
import net.skyfull.tools.listener.ItemUseListener;
import net.skyfull.tools.listener.MagnetTask;
import net.skyfull.tools.listener.MiningListener;
import net.skyfull.tools.listener.SoulboundListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/** SkyfullTools main class - utility & survival enchants and items. */
public final class SkyfullTools extends JavaPlugin {

    @Override
    public void onEnable() {
        Keys.init(this);
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new SoulboundListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);

        SkyfullCommand command = new SkyfullCommand(this);
        if (getCommand("skyfull") != null) {
            getCommand("skyfull").setExecutor(command);
            getCommand("skyfull").setTabCompleter(command);
        }

        int interval = Math.max(2, getConfig().getInt("magnet-task-interval", 8));
        getServer().getScheduler().runTaskTimer(this, new MagnetTask(this), 40L, interval);

        if (getConfig().getBoolean("enable-recipes", true)) {
            registerRecipes();
        }

        if (getConfig().getBoolean("startup-banner", true)) {
            getLogger().info("SkyfullTools enabled - " + CustomEnchantCount()
                    + " enchants, " + CustomItem.values().length + " items ready.");
        }
    }

    private int CustomEnchantCount() {
        return net.skyfull.tools.enchant.CustomEnchant.values().length;
    }

    private void registerRecipes() {
        // Ender Pouch
        shaped("ender_pouch", ItemFactory.create(CustomItem.ENDER_POUCH),
                new String[]{"LEL", "EYE", "LEL"},
                'L', Material.LEATHER, 'E', Material.ENDER_PEARL, 'Y', Material.ENDER_EYE);

        // Magnet
        shaped("magnet", ItemFactory.create(CustomItem.MAGNET),
                new String[]{" I ", "IRI", " I "},
                'I', Material.IRON_INGOT, 'R', Material.REDSTONE);

        // Green Thumb Wand
        shaped("green_thumb_wand", ItemFactory.create(CustomItem.GREEN_THUMB_WAND),
                new String[]{" L ", " S ", " B "},
                'L', Material.OAK_LEAVES, 'S', Material.STICK, 'B', Material.BONE_BLOCK);

        // Warp Stone
        shaped("warp_stone", ItemFactory.create(CustomItem.WARP_STONE),
                new String[]{"SPS", "PES", "SPS"},
                'S', Material.AMETHYST_SHARD, 'P', Material.ENDER_PEARL, 'E', Material.ECHO_SHARD);

        // Prospector's Compass
        shaped("prospectors_compass", ItemFactory.create(CustomItem.PROSPECTORS_COMPASS),
                new String[]{"GRG", "RCR", "GRG"},
                'G', Material.GOLD_NUGGET, 'R', Material.RAW_IRON, 'C', Material.COMPASS);
    }

    private void shaped(String key, org.bukkit.inventory.ItemStack result,
                        String[] shape, Object... ingredients) {
        try {
            NamespacedKey nk = new NamespacedKey(this, key);
            ShapedRecipe recipe = new ShapedRecipe(nk, result);
            recipe.shape(shape);
            for (int i = 0; i < ingredients.length; i += 2) {
                recipe.setIngredient((Character) ingredients[i], (Material) ingredients[i + 1]);
            }
            getServer().addRecipe(recipe);
        } catch (Throwable t) {
            getLogger().warning("Could not register recipe '" + key + "': " + t.getMessage());
        }
    }
}
