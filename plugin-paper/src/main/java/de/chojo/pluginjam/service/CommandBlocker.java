/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.eldoria.eldoutilities.localization.ILocalizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlocker implements Listener {
    private final List<String> blocked = List.of("/restart", "/stop", "/minecraft:stop");
    private final ServerRequests requests;
    private final ILocalizer localizer;

    public CommandBlocker(ServerRequests requests, ILocalizer localizer) {
        this.requests = requests;
        this.localizer = localizer;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        var cmd = event.getMessage();

        if ("/restart".equals(cmd) || "/spigot:restart".equals(cmd)) {
            event.getPlayer().sendMessage(Component.text(localizer.localize("commandblocked.restartrequested")));
            requests.restartByUserOrServer(true);
            event.setCancelled(true);
            return;
        }

        for (var block : blocked) {
            if (cmd.startsWith(block)) {
                event.getPlayer().sendMessage(Component.text(localizer.localize("commandblocker.blocked"), NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
        }
    }
}
