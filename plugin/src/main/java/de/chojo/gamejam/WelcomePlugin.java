/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WelcomePlugin extends JavaPlugin implements Listener {

    private Component joinMessageComponent;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        var message = getConfig().getString("message", "Welcome!");

        getServer().getPluginManager().registerEvents(this, this);
        joinMessageComponent = MiniMessage.miniMessage().deserialize(message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(joinMessageComponent);
    }
}
