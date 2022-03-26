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
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public final class Invite implements SubCommand<Jam> {
    private final TeamData teamData;
    private final JamData jamData;

    public Invite(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.reply(context.localize("error.noTeam")).setEphemeral(true).queue();
            return;
        }

        if (team.get().leader() != event.getUser().getIdLong()) {
            event.reply(context.localize("command.team.invite.noLeader")).setEphemeral(true).queue();
            return;
        }

        var member = teamData.getMember(team.get()).join();
        var settings = jamData.getJamSettings(event.getGuild()).join();

        if (member.size() >= settings.teamSize()) {
            event.reply(context.localize("error.maxTeamSize")).setEphemeral(true).queue();
            return;
        }

        var user = event.getOption("user").getAsUser();

        if (!jam.registrations().contains(user.getIdLong())) {
            event.reply(context.localize("command.team.invite.notRegistered")).setEphemeral(true).queue();
            return;
        }

        var currTeam = teamData.getTeamByMember(jam, user).join();

        if (currTeam.isPresent()) {
            event.reply(context.localize("command.team.invite.partOfTeam")).queue();
            return;
        }


        user.openPrivateChannel().queue(channel -> {
            var embed = new LocalizedEmbedBuilder(context.localizer())
                    .setTitle("command.team.invite.invited", Replacement.create("GUILD", event.getGuild().getName()))
                    .setDescription("command.team.invite.message", Replacement.createMention(event.getUser()), Replacement.create("TEAM", team.get().name()))
                    .build();
            event.reply(context.localize("command.team.invite.send")).setEphemeral(true).queue();
            context.registerButtons(embed, channel, user, ButtonEntry.of(Button.of(ButtonStyle.SUCCESS, "accept", "command.team.invite.accept"),
                    button -> accept(button, event.getGuild().getIdLong(), team.get(), user.getIdLong(), context.localizer())));
        });
    }

    private void accept(ButtonInteraction interaction, long guildId, JamTeam team, long userId, ContextLocalizer localizer) {
        teamData.getMember(team).thenAccept(members -> {
            interaction.deferReply().queue();
            var manager = interaction.getJDA().getShardManager();
            var guild = manager.getGuildById(guildId);
            var user = manager.retrieveUserById(userId).complete();
            var member = guild.retrieveMember(user).complete();
            var settings = jamData.getJamSettings(guild).join();

            if (members.size() >= settings.teamSize()) {
                interaction.getHook().editOriginal(localizer.localize("error.maxTeamSize")).queue();
                return;
            }
            var jam = jamData.getNextOrCurrentJam(guild).join();
            if (jam.isEmpty()) {
                interaction.getHook().editOriginal(localizer.localize("command.team.invite.gameJamOver")).queue();
                return;
            }

            var currTeam = teamData.getTeamByMember(jam.get(), user).join();

            if (currTeam.isPresent()) {
                interaction.getHook().editOriginal(localizer.localize("command.team.invite.alreadyMember")).queue();
                return;
            }

            teamData.joinTeam(team, member);
            guild.addRoleToMember(member, guild.getRoleById(team.roleId())).queue();
            interaction.getHook().editOriginal(localizer.localize("command.team.invite.joined")).queue();
            guild.getTextChannelById(team.textChannelId()).sendMessage(localizer.localize("command.team.invite.joinedBroadcast", Replacement.createMention(member))).queue();
        }).whenComplete(Future.error());
    }
}
