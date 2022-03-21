/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;


import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public final class Create implements SubCommand<Jam> {
    private final TeamData teamData;

    public Create(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
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

    public TeamData teamData() {
        return teamData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Create) obj;
        return Objects.equals(this.teamData, that.teamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamData);
    }

    @Override
    public String toString() {
        return "Create[" +
                "teamData=" + teamData + ']';
    }

}
