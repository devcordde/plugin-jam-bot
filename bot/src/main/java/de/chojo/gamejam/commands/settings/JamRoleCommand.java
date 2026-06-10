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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Bundle("locale")
@Interaction
public final class JamRoleCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public JamRoleCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "settings jamrole")
    public void onCommand(CommandEvent event, @Param(value = "role", type = OptionType.ROLE) Role role) {
        var guilds = commandContextProvider.guilds();
        guilds.guild(event.getGuild()).jamSettings().jamRole(role);
        event.reply("command.settings.jamrole.message.updated");
    }
}
