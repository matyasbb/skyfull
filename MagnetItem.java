package net.skyfull.tools.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skyfull.tools.Glyphs;
import net.skyfull.tools.SkyfullTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Puts the SKYFULL logo (custom-font glyph) into the player-list (TAB) header
 * and a configurable footer line, similar to branded servers.
 *
 * Disable this in config.yml (tablist.enabled: false) if you already run a
 * dedicated TAB plugin.
 */
public class TabListener implements Listener {

    private final SkyfullTools plugin;

    public TabListener(SkyfullTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        // Small delay so it applies cleanly after the join sequence.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> apply(player), 20L);
    }

    private void apply(Player player) {
        if (!player.isOnline()) {
            return;
        }
        Component header = Component.empty()
                .append(Component.newline())
                .append(Glyphs.logo())
                .append(Component.newline());

        String footerStr = plugin.getConfig().getString("tablist.footer",
                "&bmc.skyfull.net &7• &ddiscord.skyfull.net");
        Component footer = Component.newline()
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(footerStr))
                .append(Component.newline());

        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
