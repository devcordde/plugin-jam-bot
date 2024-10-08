/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class JoinService implements Listener {
    private final Plugin plugin;
    private final ServerRequests requests;
    private final ILocalizer localizer;
    private final MessageSender messageSender;

    public JoinService(Plugin plugin, ServerRequests requests, ILocalizer localizer) {
        this.plugin = plugin;
        this.requests = requests;
        this.localizer = localizer;
        this.messageSender = MessageSender.getPluginMessageSender(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getServer().getOnlinePlayers().size() < maxPlayers()) return;

        // Ops are welcome
        if (event.getPlayer().isOp()) return;

        // Silent join
        event.joinMessage(Component.empty());

        if (isSpectatorOverflow()) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            messageSender.sendMessage(event.getPlayer(), "joinservice.spectatoroverflow");
            return;
        }

        event.getPlayer().kick(Component.text(localizer.localize("joinservice.kick")));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
            requests.restartByEmpty(true);
        }
    }

    private int maxPlayers() {
        return plugin.getConfig().getInt("maxplayers", 50);
    }

    private boolean isSpectatorOverflow() {
        return plugin.getConfig().getBoolean("spectatoroverflow", false);
    }
}
