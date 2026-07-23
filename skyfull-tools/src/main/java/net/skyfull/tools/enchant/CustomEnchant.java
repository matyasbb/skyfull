package net.skyfull.tools.enchant;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The catalogue of custom enchantments. These are NOT real Minecraft registry
 * enchantments (which would need a datapack) &mdash; they are stored in each
 * item's PersistentDataContainer and rendered into the lore, while their
 * effects are driven by event listeners. This is the robust, widely used
 * approach for Paper plugins and works fully on a vanilla-compatible client.
 */
public enum CustomEnchant {

    VEIN_MINER("vein_miner", "Vein Miner", 3, NamedTextColor.AQUA,
            "Breaks a whole vein of ore at once."),
    TIMBER("timber", "Timber", 1, NamedTextColor.GREEN,
            "Fells an entire tree in one chop."),
    AUTO_SMELT("auto_smelt", "Auto Smelt", 1, NamedTextColor.GOLD,
            "Mined ores drop smelted, with bonus XP."),
    EXCAVATOR("excavator", "Excavator", 1, NamedTextColor.YELLOW,
            "Digs a 3x3 area of soft blocks."),
    REPLANTER("replanter", "Replanter", 1, NamedTextColor.DARK_GREEN,
            "Auto-replants crops when you harvest them."),
    MAGNETIC("magnetic", "Magnetic", 3, NamedTextColor.LIGHT_PURPLE,
            "Pulls nearby drops straight to you."),
    SOULBOUND("soulbound", "Soulbound", 1, NamedTextColor.DARK_PURPLE,
            "The item stays with you when you die.");

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final NamedTextColor color;
    private final String description;

    CustomEnchant(String id, String displayName, int maxLevel,
                  NamedTextColor color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.color = color;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public NamedTextColor color() {
        return color;
    }

    public String description() {
        return description;
    }

    /** Lookup by the lowercase id (used by the /skyfull enchant command). */
    public static CustomEnchant byId(String id) {
        for (CustomEnchant e : values()) {
            if (e.id.equalsIgnoreCase(id) || e.name().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }
}
