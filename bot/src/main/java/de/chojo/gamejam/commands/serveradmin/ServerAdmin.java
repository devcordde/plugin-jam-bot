package de.chojo.gamejam.commands.serveradmin;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class ServerAdmin extends SlashCommand {
    public ServerAdmin() {
        super(Slash.of("serveradmin", "Administration of team servers")
                .adminCommand()
                .group(Group.of("start", "Start servers")
                        .subCommand(SubCommand.of("all", "Start all server")
                                .handler(null))
                        .subCommand(SubCommand.of("team", "Start a team server")
                                .handler(null)
                                .argument(Argument.text("team", "team"))))
                .group(Group.of("stop", "Stop servers")
                        .subCommand(SubCommand.of("all", "Stop all server")
                                .handler(null))
                        .subCommand(SubCommand.of("team", "Stop a team server")
                                .handler(null)
                                .argument(Argument.text("team", "team"))))
                .group(Group.of("refresh", "Refresh files of the template in all servers.")
                        .subCommand(SubCommand.of("all", "Stop all server")
                                .handler(null))
                        .subCommand(SubCommand.of("team", "Stop a team server")
                                .handler(null)))
                .subCommand(SubCommand.of("list", "List servers")
                        .handler(null))
        );
    }
}
