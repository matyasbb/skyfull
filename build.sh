package net.skyfull.tools.item;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * Catalogue of custom "gadget" items. All of them share a single base material
 * ({@link Material#STICK}) and are distinguished by their CustomModelData value,
 * which the resource pack maps to a unique model + texture.
 */
public enum CustomItem {

    ENDER_POUCH("ender_pouch", "Ender Pouch", 990001, NamedTextColor.DARK_PURPLE,
            new String[]{
                    "Right-click to open your",
                    "Ender Chest from anywhere."
            }),

    MAGNET("magnet", "Magnet", 990002, NamedTextColor.LIGHT_PURPLE,
            new String[]{
                    "Pulls nearby item drops to you.",
                    "Shift + right-click to toggle."
            }),

    GREEN_THUMB_WAND("green_thumb_wand", "Green Thumb Wand", 990003, NamedTextColor.GREEN,
            new String[]{
                    "Right-click to bonemeal a whole",
                    "area of crops at once.",
                    "Uses Bone Meal from your inventory."
            }),

    WARP_STONE("warp_stone", "Warp Stone", 990004, NamedTextColor.AQUA,
            new String[]{
                    "Shift + right-click to set home.",
                    "Right-click to warp home."
            }),

    PROSPECTORS_COMPASS("prospectors_compass", "Prospector's Compass", 990005, NamedTextColor.GOLD,
            new String[]{
                    "Right-click to scan for nearby",
                    "ores and highlight them."
            });

    private final String id;
    private final String displayName;
    private final int modelData;
    private final NamedTextColor color;
    private final String[] description;

    CustomItem(String id, String displayName, int modelData,
               NamedTextColor color, String[] description) {
        this.id = id;
        this.displayName = displayName;
        this.modelData = modelData;
        this.color = color;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int modelData() {
        return modelData;
    }

    public NamedTextColor color() {
        return color;
    }

    public String[] description() {
        return description;
    }

    public Material baseMaterial() {
        return Material.STICK;
    }

    public static CustomItem byId(String id) {
        for (CustomItem i : values()) {
            if (i.id.equalsIgnoreCase(id) || i.name().equalsIgnoreCase(id)) {
                return i;
            }
        }
        return null;
    }
}
