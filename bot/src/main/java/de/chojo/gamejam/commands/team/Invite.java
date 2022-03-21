/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.buttons.ButtonEntry;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public record Invite(TeamData teamData, JamData jamData) implements SubCommand<Jam> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.reply("You are not part of the team.").setEphemeral(true).queue();
            return;
        }

        if (team.get().leader() != event.getUser().getIdLong()) {
            event.reply("Only the group leader can invite people.").setEphemeral(true).queue();
            return;
        }

        var member = teamData.getMember(team.get()).join();
        var settings = jamData.getSettings(event.getGuild()).join();

        if (member.size() >= settings.teamSize()) {
            event.reply("Your team has reached the max size.").setEphemeral(true).queue();
            return;
        }

        var user = event.getOption("user").getAsUser();

        if (!jam.registrations().contains(user.getIdLong())) {
            event.reply("This user is not registered for the game jam.").setEphemeral(true).queue();
            return;
        }

        var join = teamData.getTeamByMember(jam, user).join();

        if (join.isPresent()) {
            event.reply("This user is already part of a team").queue();
            return;
        }


        user.openPrivateChannel().queue(channel -> {
            var embed = new LocalizedEmbedBuilder(context.localizer())
                    .setTitle("You received a invitation for the game jam on " + event.getGuild().getName())
                    .setDescription(event.getUser().getName() + " invited you to join their team " + team.get().name())
                    .build();
            event.reply("Invitation send").setEphemeral(true).queue();
            context.registerButtons(embed, channel, user, ButtonEntry.of(Button.of(ButtonStyle.SUCCESS, "accept", "Accept"),
                    button -> {
                        accept(button, event.getGuild().getIdLong(), team.get(), user.getIdLong());
                    }));
        });
    }

    private void accept(ButtonInteraction interaction, long guildId, JamTeam team, long userId) {
        teamData.getMember(team).thenAccept(members -> {
            interaction.deferReply().queue();
            var manager = interaction.getJDA().getShardManager();
            var guild = manager.getGuildById(guildId);
            var user = manager.retrieveUserById(userId).complete();
            var member = guild.retrieveMember(user).complete();
            var settings = jamData.getSettings(guild).join();

            if (members.size() >= settings.teamSize()) {
                interaction.getHook().editOriginal("The team has reached the max size.").queue();
                return;
            }
            var jam = jamData.getNextOrCurrentJam(guild).join();
            if (jam.isEmpty()) {
                interaction.getHook().editOriginal("The game jam is over").queue();
                return;
            }

            var currTeam = teamData.getTeamByMember(jam.get(), user).join();

            if (currTeam.isPresent()) {
                interaction.getHook().editOriginal("This user is already part of a team").queue();
                return;
            }

            teamData.joinTeam(team, member);
            guild.addRoleToMember(member, guild.getRoleById(team.roleId())).queue();
            interaction.getHook().editOriginal("Du bist dem Team beigetreten.").queue();
            guild.getTextChannelById(team.textChannelId()).sendMessage(user.getName() + " joined the team.").queue();
        }).whenComplete(Future.error());
    }
}
