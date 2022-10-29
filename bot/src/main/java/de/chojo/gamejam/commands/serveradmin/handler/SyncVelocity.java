package de.chojo.gamejam.commands.serveradmin.handler;

import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SyncVelocity implements SlashHandler {
    private final ServerService serverService;

    public SyncVelocity(ServerService serverService) {
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        serverService.syncVelocity();
        event.reply("Synced server").queue();
    }
}
