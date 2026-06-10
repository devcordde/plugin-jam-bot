/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot;

import com.google.inject.AbstractModule;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;

public class BotModule extends AbstractModule {
    private final CommandContextProvider commandContextProvider;

    public BotModule(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Override
    protected void configure() {
        bind(CommandContextProvider.class).toInstance(commandContextProvider);
    }
}
