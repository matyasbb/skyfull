package net.skyfull.tools.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyfull.tools.SkyfullTools;
import net.skyfull.tools.enchant.CustomEnchant;
import net.skyfull.tools.enchant.EnchantUtil;
import net.skyfull.tools.item.CustomItem;
import net.skyfull.tools.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkyfullCommand implements CommandExecutor, TabCompleter {

    private final SkyfullTools plugin;

    public SkyfullCommand(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> give(sender, args);
            case "enchant" -> enchant(sender, args);
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            default -> help(sender);
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyfull.admin")) {
            deny(sender);
            return;
        }
        if (args.length < 2) {
            info(sender, "Usage: /skyfull give <item> [player]");
            return;
        }
        CustomItem type = CustomItem.byId(args[1]);
        if (type == null) {
            error(sender, "Unknown item: " + args[1]);
            return;
        }
        Player target;
        if (args.length >= 3) {
            target = plugin.getServer().getPlayerExact(args[2]);
            if (target == null) {
                error(sender, "Player not found: " + args[2]);
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            error(sender, "Specify a player from the console.");
            return;
        }
        target.getInventory().addItem(ItemFactory.create(type));
        info(sender, "Gave " + type.displayName() + " to " + target.getName() + ".");
    }

    private void enchant(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyfull.admin")) {
            deny(sender);
            return;
        }
        if (!(sender instanceof Player player)) {
            error(sender, "Only a player can enchant a held item.");
            return;
        }
        if (args.length < 2) {
            info(sender, "Usage: /skyfull enchant <enchant> [level]");
            return;
        }
        CustomEnchant ench = CustomEnchant.byId(args[1]);
        if (ench == null) {
            error(sender, "Unknown enchant: " + args[1]);
            return;
        }
        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                error(sender, "Level must be a number.");
                return;
            }
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            error(sender, "Hold an item to enchant it.");
            return;
        }
        EnchantUtil.apply(held, ench, level);
        info(sender, "Applied " + ench.displayName() + " "
                + EnchantUtil.roman(Math.min(level, ench.maxLevel())) + ".");
    }

    private void list(CommandSender sender) {
        sender.sendMessage(Component.text("SKYFULL custom enchants:", NamedTextColor.DARK_AQUA));
        for (CustomEnchant e : CustomEnchant.values()) {
            sender.sendMessage(Component.text("  " + e.id() + " ", e.color())
                    .append(Component.text("- " + e.description()
                            + " (max " + e.maxLevel() + ")", NamedTextColor.GRAY)));
        }
        sender.sendMessage(Component.text("SKYFULL custom items:", NamedTextColor.DARK_AQUA));
        for (CustomItem i : CustomItem.values()) {
            sender.sendMessage(Component.text("  " + i.id() + " ", i.color())
                    .append(Component.text("- " + i.displayName(), NamedTextColor.GRAY)));
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("skyfull.admin")) {
            deny(sender);
            return;
        }
        plugin.reloadConfig();
        info(sender, "Configuration reloaded.");
    }

    private void help(CommandSender sender) {
        sender.sendMessage(Component.text("=== SkyfullTools ===", NamedTextColor.DARK_AQUA));
        info(sender, "/skyfull give <item> [player]");
        info(sender, "/skyfull enchant <enchant> [level]");
        info(sender, "/skyfull list");
        info(sender, "/skyfull reload");
    }

    /* ---------------- tab completion ---------------- */

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            addMatches(out, args[0], "give", "enchant", "list", "reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (CustomItem i : CustomItem.values()) {
                addMatch(out, args[1], i.id());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("enchant")) {
            for (CustomEnchant e : CustomEnchant.values()) {
                addMatch(out, args[1], e.id());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                addMatch(out, args[2], p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("enchant")) {
            addMatches(out, args[2], "1", "2", "3");
        }
        return out;
    }

    private void addMatches(List<String> out, String prefix, String... options) {
        for (String o : options) {
            addMatch(out, prefix, o);
        }
    }

    private void addMatch(List<String> out, String prefix, String option) {
        if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
            out.add(option);
        }
    }

    /* ---------------- message helpers ---------------- */

    private void info(CommandSender s, String text) {
        s.sendMessage(Component.text("[SKYFULL] ", NamedTextColor.DARK_AQUA)
                .append(Component.text(text, NamedTextColor.GRAY)));
    }

    private void error(CommandSender s, String text) {
        s.sendMessage(Component.text("[SKYFULL] ", NamedTextColor.DARK_AQUA)
                .append(Component.text(text, NamedTextColor.RED)));
    }

    private void deny(CommandSender s) {
        error(s, "You don't have permission for that.");
    }
}
