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
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class JamAdmin extends SlashCommand {


    public JamAdmin(Guilds guilds) {
        super(Slash.of("jamadmin", "command.jamadmin.description")
                .adminCommand()
                .subCommand(SubCommand.of("create", "command.jamadmin.create.description")
                        .handler(new Create(guilds))
                        .argument(Argument.text("topic", "command.jamadmin.create.options.topic.description")
                                          .asRequired())
                        .argument(Argument.text("tagline", "command.jamadmin.create.options.tagline.description")
                                          .asRequired())
                        .argument(Argument.text("timezone", "command.jamadmin.create.options.timezone.description")
                                          .asRequired()
                                          .withAutoComplete())
                        .argument(Argument.text("registerstart", "command.jamadmin.create.options.registerstart.description")
                                          .asRequired())
                        .argument(Argument.text("registerend", "command.jamadmin.create.options.registerend.description")
                                          .asRequired())
                        .argument(Argument.text("jamstart", "command.jamadmin.create.options.jamstart.description")
                                          .asRequired())
                        .argument(Argument.text("jamend", "command.jamadmin.create.options.jamend.description")
                                          .asRequired()))
                .group(Group.of("jam", "command.jamadmin.jam.description")
                        .subCommand(SubCommand.of("start", "command.jamadmin.jam.start.description")
                                .handler(new JamStart(guilds)))
                        .subCommand(SubCommand.of("end", "command.jamadmin.jam.end.description")
                                .handler(new JamEnd(guilds))
                                .argument(Argument.bool("confirm", "command.jamadmin.jam.end.options.confirm.description"))))
                .group(Group.of("votes", "command.jamadmin.votes.description")
                        .subCommand(SubCommand.of("open", "command.jamadmin.votes.open.description")
                                .handler(new ChangeVotes(guilds, true, "command.jamadmin.votes.message.opened")))
                        .subCommand(SubCommand.of("close", "command.jamadmin.votes.close.description")
                                .handler(new ChangeVotes(guilds, false, "command.jamadmin.votes.message.closed"))))
                .build());
    }
}
