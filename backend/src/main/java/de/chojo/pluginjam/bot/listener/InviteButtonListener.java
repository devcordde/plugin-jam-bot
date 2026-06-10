/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.listener;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class InviteButtonListener extends ListenerAdapter {
    /*private static final Logger log = getLogger(InviteButtonListener.class);
    private static final String BUTTON_PREFIX = "invite-accept:";

    private final Guilds guilds;

    public InviteButtonListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        var componentId = event.getComponentId();
        if (!componentId.startsWith(BUTTON_PREFIX)) {
            return;
        }

        event.deferReply().queue();

        var parts = componentId.substring(BUTTON_PREFIX.length()).split(":");
        if (parts.length != 3) {
            event.getHook().editOriginal("Invalid invite data.").queue();
            return;
        }

        long guildId;
        int teamId;
        long userId;
        try {
            guildId = Long.parseLong(parts[0]);
            teamId = Integer.parseInt(parts[1]);
            userId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            event.getHook().editOriginal("Invalid invite data.").queue();
            return;
        }

        var manager = event.getJDA().getShardManager();
        var guild = manager.getGuildById(guildId);
        if (guild == null) {
            event.getHook().editOriginal("Guild not found.").queue();
            return;
        }

        var user = manager.retrieveUserById(userId).complete();
        var member = guild.retrieveMember(user).complete();
        var jamGuild = guilds.guild(guild);
        var settings = jamGuild.jamSettings();

        var optJam = jamGuild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.getHook().editOriginal("The game jam is over.").queue();
            return;
        }

        var jam = optJam.get();
        var optTeam = jam.teams().byId(teamId);
        if (optTeam.isEmpty()) {
            event.getHook().editOriginal("Team not found.").queue();
            return;
        }

        var team = optTeam.get();
        var members = team.member();

        if (members.size() >= settings.teamSize()) {
            event.getHook().editOriginal("The team is already full.").queue();
            return;
        }

        var currTeam = jam.teams().byMember(user);
        if (currTeam.isPresent()) {
            event.getHook().editOriginal("You are already part of a team.").queue();
            return;
        }

        jam.user(member).join(team);
        team.meta().role().ifPresent(role -> guild.addRoleToMember(member, role).queue());
        event.getHook().editOriginal("You have joined the team!").queue();
        team.meta().textChannel().ifPresent(channel ->
                channel.sendMessage(member.getAsMention() + " has joined the team!").queue()
        );

        // Disable the button after use
        event.getMessage().editMessageComponents().queue();
    }

     */
}
