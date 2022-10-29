package de.chojo.pluginjam.service;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinService implements Listener {
    private final Plugin plugin;

    public JoinService(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getServer().getOnlinePlayers().size() < maxPlayers()) return;

        // Ops are welcome
        if (event.getPlayer().isOp()) return;

        // Silent join
        event.joinMessage(Component.empty());

        if (isSpectatorOverflow()) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            return;
        }

        event.getPlayer().kick(Component.text("Server is full."));
    }

    private int maxPlayers() {
        return plugin.getConfig().getInt("maxplayers", 50);
    }

    private boolean isSpectatorOverflow() {
        return plugin.getConfig().getBoolean("spectatoroverflow", false);
    }
}
