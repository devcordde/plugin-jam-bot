/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.buttons.ButtonEntry;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Team extends SimpleCommand {
    private final TeamData teamData;
    private final JamData jamData;
    private static final Logger log = getLogger(Team.class);

    public Team(TeamData teamData, JamData jamData) {
        super(CommandMeta.builder("team", "Manage your team")
                .addSubCommand("create", "Create a team",
                        argsBuilder()
                                .add(SimpleArgument.string("name", "Name of your team").asRequired())
                                .build())
                .addSubCommand("invite", "Invite someone",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "The user you want to invite").asRequired())
                                .build())
                .addSubCommand("leave", "Leave your team")
                .addSubCommand("disband", "Disband your team",
                        argsBuilder().add(SimpleArgument.bool("confirm", "confirm with true").asRequired()).build())
//                .addSubCommand("new-leader", "Pass team leadership to someone else",
//                        argsBuilder()
//                                .add(SimpleArgument.user("new_leader", "The new leader").asRequired())
//                                .build())
                .addSubCommand("profile", "Shows the profile of a team or your own",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "Show the team of the user"))
                                .add(SimpleArgument.string("team", "Show the team profile").withAutoComplete())
                                .build())
                .build());
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getNextOrCurrentJam(event.getGuild())
                .thenAccept(optJam -> {
                    if (optJam.isEmpty()) {
                        event.reply("No jam is in progress. Teams are not available.").setEphemeral(true).queue();
                        return;
                    }
                    if ("create".equals(event.getSubcommandName())) {
                        create(optJam.get(), event, context);
                        return;
                    }

                    if ("invite".equals(event.getSubcommandName())) {
                        invite(optJam.get(), event, context);
                        return;
                    }

                    if ("leave".equals(event.getSubcommandName())) {
                        leave(optJam.get(), event, context);
                        return;
                    }

                    if ("disband".equals(event.getSubcommandName())) {
                        disband(optJam.get(), event, context);
                        return;
                    }

                    if ("profile".equals(event.getSubcommandName())) {
                        profile(optJam.get(), event, context);
                    }
                }).whenComplete(Future.error());
    }

    private void profile(Jam jam, SlashCommandInteractionEvent event, SlashCommandContext context) {
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

    private void disband(Jam jam, SlashCommandInteractionEvent event, SlashCommandContext context) {
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

    private void leave(Jam jam, SlashCommandInteractionEvent event, SlashCommandContext context) {
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.reply("You are not part of a team").setEphemeral(true).queue();
            return;
        }
        event.reply("You left the team").setEphemeral(true).queue();
        teamData.leaveTeam(team.get(), event.getMember());
        event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(team.get().roleId())).queue();
        event.getGuild().getTextChannelById(team.get().textChannelId()).sendMessage(event.getUser().getName() + " left the team.").queue();
    }

    private void invite(Jam jam, SlashCommandInteractionEvent event, SlashCommandContext context) {
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

    private void create(Jam jam, SlashCommandInteractionEvent event, SlashCommandContext context) {
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isPresent()) {
            event.reply("You are already part of a team. You need to leave first to create your own team.").setEphemeral(true).queue();
            return;
        }
        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply("You need to register first to create a team").setEphemeral(true).queue();
            return;
        }
        var teamName = event.getOption("name").getAsString();
        var optTeam = teamData.getTeamByName(jam, event.getOption("name").getAsString()).join();

        if (optTeam.isPresent()) {
            event.reply("This team name is already taken.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();

        var categoryList = event.getGuild().getCategoriesByName("Team", true);

        var optCategory = categoryList.stream().filter(cat -> cat.getChannels().size() < 48).findFirst();
        // This is really hacky and I dont like it.
        // All this stuff is blocking atm but in a different thread already
        var category = optCategory.orElseGet(() -> event.getGuild().createCategory("Team").complete());

        var role = event.getGuild()
                .createRole()
                .setPermissions(0L)
                .setMentionable(false)
                .setHoisted(false)
                .setName(teamName)
                .complete();

        var text = event.getGuild().createTextChannel(teamName.replace(" ", "-"), category)
                .addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet())
                .addMemberPermissionOverride(event.getJDA().getSelfUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                .addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();

        var voice = event.getGuild().createVoiceChannel(teamName, category)
                .addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet())
                .addMemberPermissionOverride(event.getJDA().getSelfUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                .addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();

        var newTeam = JamTeam.create(teamName, event.getMember(), role, text, voice);

        teamData.createTeam(jam, newTeam);

        event.getGuild().addRoleToMember(event.getMember(), role).queue();
        event.getHook().editOriginal("You team was created.").queue();
    }
}
