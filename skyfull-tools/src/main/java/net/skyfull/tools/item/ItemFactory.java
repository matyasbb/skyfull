package net.skyfull.tools.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyfull.tools.Keys;
import net.skyfull.tools.enchant.EnchantUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Builds concrete {@link ItemStack}s for the custom gadget items. */
public final class ItemFactory {

    private ItemFactory() {
    }

    public static ItemStack create(CustomItem type) {
        ItemStack item = new ItemStack(type.baseMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(type.displayName(), type.color())
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        for (String line : type.description()) {
            lore.add(EnchantUtil.gray(line));
        }
        // Small tag line so the item feels part of a set.
        lore.add(Component.text("SKYFULL", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);

        meta.getPersistentDataContainer()
                .set(Keys.ITEM_TYPE, PersistentDataType.STRING, type.id());

        meta.setCustomModelData(type.modelData());
        meta.setEnchantmentGlintOverride(Boolean.TRUE);

        try {
            meta.setMaxStackSize(1);
        } catch (Throwable ignored) {
            // Older API without per-item stack size; harmless.
        }

        item.setItemMeta(meta);

        if (type == CustomItem.MAGNET) {
            MagnetItem.setEnabled(item, false); // start switched off
        }
        return item;
    }

    /** Reads the custom item type stored on an ItemStack, or null. */
    public static CustomItem typeOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(Keys.ITEM_TYPE, PersistentDataType.STRING);
        return id == null ? null : CustomItem.byId(id);
    }
}
