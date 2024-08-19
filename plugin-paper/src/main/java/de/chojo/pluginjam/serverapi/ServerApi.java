/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.serverapi;

import de.chojo.pluginjam.service.ServerRequests;
import org.bukkit.plugin.Plugin;

public class ServerApi {
    private final Plugin plugin;
    private final ServerRequests requests;

    public ServerApi(Plugin plugin, ServerRequests requests) {
        this.plugin = plugin;
        this.requests = requests;
    }

    /**
     * Requests a restart of the server
     */
    public void requestRestart() {
        requests.restartByUserOrServer(true);
    }

    /**
     * Sets the join message of this server
     * @param message
     */
    public void setMessage(String message) {
        plugin.getConfig().set("message", message);
        plugin.saveConfig();
    }
}
