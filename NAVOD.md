package net.skyfull.tools.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyfull.tools.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Helpers for the Magnet gadget's on/off state and its display name. */
public final class MagnetItem {

    private MagnetItem() {
    }

    public static boolean isEnabled(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Byte b = item.getItemMeta().getPersistentDataContainer()
                .get(Keys.MAGNET_ON, PersistentDataType.BYTE);
        return b != null && b == 1;
    }

    public static void setEnabled(ItemStack item, boolean on) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer()
                .set(Keys.MAGNET_ON, PersistentDataType.BYTE, (byte) (on ? 1 : 0));

        Component state = on
                ? Component.text(" (ON)", NamedTextColor.GREEN)
                : Component.text(" (OFF)", NamedTextColor.RED);
        meta.displayName(Component.text("Magnet", NamedTextColor.LIGHT_PURPLE)
                .append(state)
                .decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(meta);
    }
}
