package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public record Profile(TeamData teamData) implements SubCommand<Jam> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        if (event.getOption("user") != null) {
            var team = teamData.getTeamByMember(jam, event.getOption("user").getAsMember()).join();
            if (team.isEmpty()) {
                event.reply("This user is not part of a team").setEphemeral(true).queue();
                return;
            }
            sendProfile(event, team.get(), context);
            return;
        }
        if (event.getOption("team") != null) {
            var team = teamData.getTeamByName(jam, event.getOption("team").getAsString()).join();
            if (team.isEmpty()) {
                event.reply("This team does not exist").setEphemeral(true).queue();
                return;
            }
            sendProfile(event, team.get(), context);
            return;
        }
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.reply("You are not part of a team").setEphemeral(true).queue();
            return;
        }
        sendProfile(event, team.get(), context);
    }

    private void sendProfile(SlashCommandInteractionEvent event, JamTeam team, SlashCommandContext context) {
        var member = teamData.getMember(team).join().stream()
                .map(u -> MentionUtil.user(u.userId()))
                .collect(Collectors.joining(", "));

        var embed = new LocalizedEmbedBuilder(context.localizer())
                .setTitle(team.name())
                .addField("Member", member, true)
                .addField("Leader", MentionUtil.user(team.leader()), true)
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
