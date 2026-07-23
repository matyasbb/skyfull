package net.skyfull.tools.enchant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyfull.tools.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes custom enchant levels on items and keeps the lore in sync.
 * Enchant lore lines are prefixed with {@code ✦ } so they can be identified
 * and rebuilt without disturbing an item's own description lines.
 */
public final class EnchantUtil {

    public static final String MARK = "✦ "; // "✦ "

    private EnchantUtil() {
    }

    public static int getLevel(ItemStack item, CustomEnchant ench) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer lvl = pdc.get(Keys.enchant(ench.id()), PersistentDataType.INTEGER);
        return lvl == null ? 0 : lvl;
    }

    public static boolean has(ItemStack item, CustomEnchant ench) {
        return getLevel(item, ench) > 0;
    }

    public static Map<CustomEnchant, Integer> getAll(ItemStack item) {
        Map<CustomEnchant, Integer> out = new EnumMap<>(CustomEnchant.class);
        if (item == null || !item.hasItemMeta()) {
            return out;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (CustomEnchant e : CustomEnchant.values()) {
            Integer lvl = pdc.get(Keys.enchant(e.id()), PersistentDataType.INTEGER);
            if (lvl != null && lvl > 0) {
                out.put(e, lvl);
            }
        }
        return out;
    }

    /** Apply (or, with level 0, remove) an enchant and refresh the item's lore. */
    public static void apply(ItemStack item, CustomEnchant ench, int level) {
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int clamped = Math.max(0, Math.min(level, ench.maxLevel()));
        if (clamped <= 0) {
            pdc.remove(Keys.enchant(ench.id()));
        } else {
            pdc.set(Keys.enchant(ench.id()), PersistentDataType.INTEGER, clamped);
        }
        rebuildLore(meta);
        item.setItemMeta(meta);
    }

    /** Rebuilds enchant lore lines from the PDC and toggles the glint. */
    public static void rebuildLore(ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Keep any non-enchant (description) lore the item already had.
        List<Component> kept = new ArrayList<>();
        List<Component> existing = meta.lore();
        if (existing != null) {
            PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
            for (Component line : existing) {
                if (!plain.serialize(line).startsWith(MARK)) {
                    kept.add(line);
                }
            }
        }

        List<Component> enchantLines = new ArrayList<>();
        boolean any = false;
        for (CustomEnchant e : CustomEnchant.values()) {
            Integer lvl = pdc.get(Keys.enchant(e.id()), PersistentDataType.INTEGER);
            if (lvl != null && lvl > 0) {
                any = true;
                Component line = Component.text(MARK, e.color())
                        .append(Component.text(e.displayName(), e.color()));
                if (e.maxLevel() > 1) {
                    line = line.append(Component.text(" " + roman(lvl), e.color()));
                }
                enchantLines.add(line.decoration(TextDecoration.ITALIC, false));
            }
        }

        List<Component> lore = new ArrayList<>();
        lore.addAll(enchantLines);
        lore.addAll(kept);
        meta.lore(lore.isEmpty() ? null : lore);

        meta.setEnchantmentGlintOverride(any ? Boolean.TRUE : null);
    }

    public static Component gray(String text) {
        return Component.text(text, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String roman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return Integer.toString(n);
        }
    }
}
