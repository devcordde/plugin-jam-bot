/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Api implements SlashHandler {
    private final Configuration configuration;
    private final Guilds guilds;

    public Api(Configuration configuration, Guilds guilds) {
        this.configuration = configuration;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var optTeam = optJam.get().teams().byMember(event.getMember());
        if (optTeam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        MessageEmbed build = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("Api")
                .addField("Token", "`%s`".formatted(optTeam.get().meta().token()), false)
                .addField("Upload", "`POST %s/api/v1/server/plugin/%s`".formatted(configuration.api().url(), optTeam.get().meta().token()), false)
                .build();

        event.replyEmbeds(build).setEphemeral(true).queue();
    }
}
