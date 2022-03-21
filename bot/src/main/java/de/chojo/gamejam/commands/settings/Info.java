package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Info implements SubCommand<JamSettings> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var embed = new LocalizedEmbedBuilder(context.localizer())
                .setTitle("Settings")
                .addField("Game Jam Role", MentionUtil.role(settings.jamRole()), true)
                .addField("Max Team Size", String.valueOf(settings.teamSize()), true)
                .addField("Orga Role", MentionUtil.role(settings.orgaRole()), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
