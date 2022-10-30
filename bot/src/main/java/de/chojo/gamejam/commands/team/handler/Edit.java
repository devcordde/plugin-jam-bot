/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class Edit implements SlashHandler {
    private final Guilds guilds;

    public Edit(Guilds guilds) {
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

        var meta = optTeam.get().meta();

        context.registerModal(ModalHandler.builder("Edit Profile")
                .addInput(TextInputHandler.builder("descr", "Project Description", TextInputStyle.PARAGRAPH)
                        .withValue(meta.projectDescription())
                        .withMaxLength(100)
                        .withHandler(mapping -> meta.projectDescription(mapping.getAsString())))
                .addInput(TextInputHandler.builder("url", "Project url", TextInputStyle.SHORT)
                        .withMaxLength(200)
                        .withValue(meta.projectUrl())
                        .withHandler(mapping -> meta.projectUrl(mapping.getAsString())))
                .withHandler(modalEvent -> {
                    modalEvent.reply("Updated").queue();
                })
                .build());
    }
}
