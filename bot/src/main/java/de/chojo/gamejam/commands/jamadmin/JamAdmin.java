/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin;

import de.chojo.gamejam.commands.jamadmin.handler.Create;
import de.chojo.gamejam.commands.jamadmin.handler.jam.JamEnd;
import de.chojo.gamejam.commands.jamadmin.handler.jam.JamStart;
import de.chojo.gamejam.commands.jamadmin.handler.votes.ChangeVotes;
import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class JamAdmin extends SlashCommand {


    public JamAdmin(JamData jamData) {
        super(Slash.of("jamadmin", "command.jamadmin.description")
                .adminCommand()
                .subCommand(SubCommand.of("create", "command.jamadmin.create.description")
                        .handler(new Create(jamData))
                        .argument(Argument.text("topic", "command.jamadmin.create.topic.description").asRequired())
                        .argument(Argument.text("tagline", "command.jamadmin.create.tagline.description").asRequired())
                        .argument(Argument.text("timezone", "command.jamadmin.create.timezone.description").asRequired()
                                          .withAutoComplete())
                        //TODO: Get rid of this mess
                        .argument(Argument.text("registerstart", formatArg("command.jamadmin.create.arg.registerStart"))
                                          .asRequired())
                        .argument(Argument.text("registerend", formatArg("command.jamadmin.create.arg.registerEnd"))
                                          .asRequired())
                        .argument(Argument.text("jamstart", formatArg("command.jamadmin.create.arg.jamStart"))
                                          .asRequired())
                        .argument(Argument.text("jamend", formatArg("command.jamadmin.create.arg.jamEnd")).asRequired())
                )
                .group(Group.of("jam", "command.jamadmin.jam.description")
                        .subCommand(SubCommand.of("start", "command.jamadmin.jamstart.description")
                                .handler(new JamStart(jamData)))
                        .subCommand(SubCommand.of("end", "command.jamadmin.jamend.description")
                                .handler(new JamEnd(jamData))
                                .argument(Argument.bool("confirm", "command.jamadmin.end.confirm.description"))))
                .group(Group.of("votes", "command.jamadmin.votes.description")
                        .subCommand(SubCommand.of("open", "command.jamadmin.openvotes.description")
                                .handler(new ChangeVotes(jamData, true, "command.jamAdmin.vote.open")))
                        .subCommand(SubCommand.of("close", "command.jamadmin.close-votes.description")
                                .handler(new ChangeVotes(jamData, false, "command.jamAdmin.vote.close"))))
                .build());
    }

    private static String formatArg(String key) {
        return String.format("$%s$ $%s$: %s", key, "command.jamadmin.create.arg.format", Create.PATTERN);
    }
}
