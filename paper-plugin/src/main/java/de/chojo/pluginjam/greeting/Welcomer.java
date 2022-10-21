/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.greeting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class Welcomer implements Listener {
    private final Component welcomeMessage;

    public Welcomer(Plugin plugin) {
        var message = plugin.getConfig().getString("message", "Welcome!");
        welcomeMessage = MiniMessage.miniMessage().deserialize(message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(welcomeMessage);
    }

}
