/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam;

import de.chojo.pluginjam.api.Api;
import de.chojo.pluginjam.greeting.Welcomer;
import de.chojo.pluginjam.serverapi.ServerApi;
import de.chojo.pluginjam.service.CommandBlocker;
import de.chojo.pluginjam.service.JoinService;
import de.chojo.pluginjam.service.ServerRequests;
import de.chojo.pluginjam.velocity.ReportService;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import org.bukkit.event.Listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PluginJam extends EldoPlugin implements Listener {
    private Api api;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ReportService service;
    private ServerApi serverApi;

    @Override
    public void onPluginEnable() {
        saveDefaultConfig();

        var localizer = ILocalizer.create(this, "de_DE");
        localizer.setLocale("de_DE");

        var serverRequests = new ServerRequests();

        api = Api.create(this, serverRequests);
        service = ReportService.create(this, executor);
        serverApi = new ServerApi(this, serverRequests);

        registerListener(new CommandBlocker(serverRequests, localizer), new Welcomer(this), new JoinService(this, serverRequests, localizer));
    }

    @Override
    public void onPluginDisable() {
        service.shutdown();
        executor.shutdown();
    }

    public ServerApi api() {
        return serverApi;
    }
}
