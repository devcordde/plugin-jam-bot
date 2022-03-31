package de.chojo.gamejam.commands.vote;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Vote implements SubCommand<Jam> {
    private final TeamData teamData;

    public Vote(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        event.deferReply(true).queue();
        if (!jam.state().isVoting()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.notActive")).queue();
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.getHook().editOriginal(context.localize("error.notRegistered")).queue();
            return;
        }

        var voteTeam = jam.teams().stream()
                .filter(t -> t.name().equalsIgnoreCase(event.getOption("name").getAsString()))
                .findFirst();

        if (voteTeam.isEmpty()) {
            event.getHook().editOriginal(context.localize("error.unkownTeam")).queue();
            return;
        }

        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.noTeam")).queue();
            return;
        }

        if (team.get().name().equalsIgnoreCase(event.getOption("team").getAsString())) {
            event.getHook().editOriginal("command.votes.vote.ownTeam").queue();
            return;
        }

        var pointsGiven = teamData.votesByUser(event.getMember(), jam).join().stream().mapToInt(VoteEntry::points).sum();

        //TODO: Max points and max points per team are currently hardcoded. should be configurable in the future.
        var points = Math.min(5, Math.max(0, event.getOption("points").getAsInt()));

        if (pointsGiven + points > jam.teams().size()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.maxPointsReached",
                            Replacement.create("REMAINING", jam.teams().size() - pointsGiven).addFormatting(Format.BOLD)))
                    .queue();
            return;
        }

        teamData.vote(event.getMember(), voteTeam.get(), points)
                .thenRun(() -> event.getHook().editOriginal(context.localize("command.votes.vote.done",
                                Replacement.create("REMAINING", jam.teams().size() - pointsGiven).addFormatting(Format.BOLD),
                                Replacement.create("POINTS", points).addFormatting(Format.BOLD),
                                Replacement.create("TEAM", voteTeam.get().name()).addFormatting(Format.BOLD)))
                        .queue()
                );
    }
}
