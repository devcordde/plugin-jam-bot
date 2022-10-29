package de.chojo.gamejam.commands.server.configure;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.slf4j.LoggerFactory.getLogger;

public class MaxPlayers implements SlashHandler {
    private final Server server;

    public MaxPlayers(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;

        var request = optServer.get().requestBuilder("v1/config/maxplayers")
                               .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(event.getOption("amount").getAsInt())))
                               .build();
        optServer.get().http().sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }
}
