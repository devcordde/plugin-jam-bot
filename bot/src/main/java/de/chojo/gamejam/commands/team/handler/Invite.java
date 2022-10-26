/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.ButtonEntry;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public final class Invite implements SlashHandler {
    private final Guilds guilds;

    public Invite(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        JamGuild guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("error.votingactive")).setEphemeral(true).queue();
            return;
        }

        var optTeam = jam.teams().byMember(event.getMember());
        if (optTeam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = optTeam.get();

        if (!team.isLeader(event.getUser())) {
            event.reply(context.localize("command.team.invite.message.noleader")).setEphemeral(true).queue();
            return;
        }

        var member = team.member();
        var settings = guild.jamSettings();

        if (member.size() >= settings.teamSize()) {
            event.reply(context.localize("error.maxteamsize")).setEphemeral(true).queue();
            return;
        }

        var user = event.getOption("user").getAsUser();

        if (!jam.registrations().contains(user.getIdLong())) {
            event.reply(context.localize("command.team.invite.message.notRegistered")).setEphemeral(true).queue();
            return;
        }

        var currTeam = jam.teams().byMember(user);

        if (currTeam.isPresent()) {
            event.reply(context.localize("command.team.invite.message.partofteam")).queue();
            return;
        }

        user.openPrivateChannel().queue(channel -> {
            var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("command.team.invite.message.invited", Replacement.create("GUILD", event.getGuild()
                                                                                                      .getName()))
                    .setDescription("command.team.invite.message.invitation",
                            Replacement.createMention(event.getUser()), Replacement.create("TEAM", team.meta().name()))
                    .build();
            event.reply(context.localize("command.team.invite.message.send")).setEphemeral(true).queue();
            context.registerMenu(MenuAction.forChannel(embed, channel)
                                           .addComponent(ButtonEntry.of(Button.of(ButtonStyle.SUCCESS, "accept", "command.team.invite.message.accept"),
                                                   button -> accept(button, event.getGuild().getIdLong(),
                                                           team, user.getIdLong(), context.guildLocalizer())))
                                           .build());
        });
    }

    private void accept(EntryContext<ButtonInteractionEvent, Button> button, long guildId, Team team, long userId, LocalizationContext localizer) {
        var members = team.member();
        var interaction = button.event();
        interaction.deferReply().queue();
        var manager = interaction.getJDA().getShardManager();
        var guild = manager.getGuildById(guildId);
        var user = manager.retrieveUserById(userId).complete();
        var member = guild.retrieveMember(user).complete();
        var jamGuild = guilds.guild(button.event());
        var settings = jamGuild.jamSettings();

        if (members.size() >= settings.teamSize()) {
            interaction.getHook().editOriginal(localizer.localize("error.maxteamsize")).queue();
            return;
        }
        var jam = jamGuild.jams().nextOrCurrent();
        if (jam.isEmpty()) {
            interaction.getHook().editOriginal(localizer.localize("command.team.invite.gameJamOver")).queue();
            return;
        }

        var currTeam = jam.get().teams().byMember(user);

        if (currTeam.isPresent()) {
            interaction.getHook().editOriginal(localizer.localize("command.team.invite.alreadyMember")).queue();
            return;
        }

        jam.get().user(member).join(team);
        team.meta().role().ifPresent(role -> guild.addRoleToMember(member, role));
        interaction.getHook().editOriginal(localizer.localize("command.team.invite.joined")).queue();
        team.meta().textChannel().ifPresent(channel -> {
            channel.sendMessage(localizer.localize("command.team.invite.joinedBroadcast", Replacement.createMention(member)))
                   .queue();
        });
    }
}
