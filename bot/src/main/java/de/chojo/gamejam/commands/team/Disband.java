package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record Disband(TeamData teamData) implements SubCommand<Jam> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        if (event.getOption("confirm").getAsBoolean()) {
            event.reply("Please confirm.").setEphemeral(true).queue();
            return;
        }

        var jamTeam = teamData.getTeamByMember(jam, event.getMember()).join();
        if (jamTeam.isEmpty()) {
            event.reply("You are not part of a team").setEphemeral(true).queue();
            return;
        }
        var members = teamData.getMember(jamTeam.get()).join();
        for (var teamMember : members) {
            event.getJDA().getShardManager().retrieveUserById(teamMember.userId())
                    .flatMap(u -> event.getGuild().retrieveMember(u))
                    .queue(member -> {
                        event.getGuild().removeRoleFromMember(member, event.getGuild().getRoleById(jamTeam.get().roleId())).queue();
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Your team was disbanded")).queue();
                    });
        }
        event.reply("Team disbanded").setEphemeral(true).queue();
        jamTeam.get().delete(event.getGuild());
        teamData.disbandTeam(jamTeam.get());
    }
}
