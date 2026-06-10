/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam;

import com.google.inject.AbstractModule;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.CommandContextProvider;

public class BotModule extends AbstractModule {
    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    public BotModule(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Override
    protected void configure() {
        bind(ServerService.class).toInstance(serverService);
        bind(CommandContextProvider.class).toInstance(commandContextProvider);
    }
}
