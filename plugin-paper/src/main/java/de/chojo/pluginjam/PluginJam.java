/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam;

import de.chojo.pluginjam.api.Api;
import de.chojo.pluginjam.greeting.Welcomer;
import de.chojo.pluginjam.velocity.ReportService;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PluginJam extends JavaPlugin implements Listener {
    private Api api;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ReportService service;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new Welcomer(this), this);

        api = Api.create(this);
        service = ReportService.create(this, executor);
    }

    @Override
    public void onDisable() {
        service.shutdown();
        executor.shutdown();
    }
}
