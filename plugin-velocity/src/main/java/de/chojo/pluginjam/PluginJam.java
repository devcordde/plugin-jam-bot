/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.chojo.pluginjam.servers.ServerRegistry;
import de.chojo.pluginjam.web.Api;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

@Plugin(id = "pluginjam", name = "Plugin Jam", version = "1.0.0", authors = {"RainbowdashLabs"})
public class PluginJam {
    private static final Logger log = getLogger(PluginJam.class);
    private final ProxyServer proxy;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Api api;

    @Inject
    public PluginJam(ProxyServer proxy) {
        this.proxy = proxy;
        log.info("Plugin Jam enabled");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        var registry = ServerRegistry.create(proxy, executor);

        api = Api.create(registry);
    }
}
