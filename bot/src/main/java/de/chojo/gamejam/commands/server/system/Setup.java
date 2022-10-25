package de.chojo.gamejam.commands.server.system;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Setup implements SlashHandler {
    private final ServerService serverService;
    private final TeamData teamData;
    private final JamData jamData;

    public Setup(ServerService serverService, TeamData teamData, JamData jamData) {
        this.serverService = serverService;
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {

        teamData.getTeamByMember()
    }
}
