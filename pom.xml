package net.skyfull.tools;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Central holder for every {@link NamespacedKey} the plugin writes into item
 * PersistentDataContainers, so the strings live in exactly one place.
 */
public final class Keys {

    private Keys() {
    }

    // Marks the "type" of a custom gadget item (ender_pouch, magnet, ...).
    public static NamespacedKey ITEM_TYPE;

    // Toggle state (0/1) for the Magnet gadget.
    public static NamespacedKey MAGNET_ON;

    // Warp Stone home location, stored on the player's own PDC.
    public static NamespacedKey HOME_WORLD;
    public static NamespacedKey HOME_X;
    public static NamespacedKey HOME_Y;
    public static NamespacedKey HOME_Z;

    // Prefix for enchant keys, e.g. skyfull:ench_vein_miner -> level (int).
    private static final String ENCH_PREFIX = "ench_";

    private static Plugin plugin;

    public static void init(Plugin pl) {
        plugin = pl;
        ITEM_TYPE = new NamespacedKey(pl, "item_type");
        MAGNET_ON = new NamespacedKey(pl, "magnet_on");
        HOME_WORLD = new NamespacedKey(pl, "home_world");
        HOME_X = new NamespacedKey(pl, "home_x");
        HOME_Y = new NamespacedKey(pl, "home_y");
        HOME_Z = new NamespacedKey(pl, "home_z");
    }

    /** Key used to store the level of a given custom enchant on an item. */
    public static NamespacedKey enchant(String enchantId) {
        return new NamespacedKey(plugin, ENCH_PREFIX + enchantId);
    }
}
