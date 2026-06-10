/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import com.google.inject.Inject;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Bundle("locale")
@Interaction
public final class TeamSizeCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamSizeCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "settings teamsize")
    public void onCommand(CommandEvent event, @Param("size") int size) {
        var settings = commandContextProvider.guilds().guild(event).jamSettings();
        settings.teamSize(size);
        event.with().ephemeral(true).reply("command.settings.teamsize.message.updated");
    }
}
