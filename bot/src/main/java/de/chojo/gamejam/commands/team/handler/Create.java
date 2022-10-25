/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;


import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.EnumSet;

public final class Create implements SlashHandler {
    private final Guilds guilds;

    public Create(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var jamGuild = guilds.guild(event.getGuild());
        var optJam = jamGuild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("error.votingactive")).setEphemeral(true).queue();
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply(context.localize("command.team.create.message.unregistered")).setEphemeral(true).queue();
            return;
        }
        var userTeam = jam.teams().byMember(event.getMember());
        if (userTeam.isPresent()) {
            event.reply(context.localize("command.team.create.message.alreadymember")).setEphemeral(true).queue();
            return;
        }
        var teamName = event.getOption("name").getAsString();

        // TODO: Enforce constrains of length and allowed chars

        var optTeam = jam.teams().byName(event.getOption("name").getAsString());

        if (optTeam.isPresent()) {
            event.reply(context.localize("command.team.create.message.nametaken")).setEphemeral(true).queue();
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
                        .addMemberPermissionOverride(event.getJDA().getSelfUser()
                                                          .getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                        .addRolePermissionOverride(event.getGuild().getPublicRole()
                                                        .getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                        .complete();

        var voice = event.getGuild().createVoiceChannel(teamName, category)
                         .addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet())
                         .addMemberPermissionOverride(event.getJDA().getSelfUser()
                                                           .getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                         .addRolePermissionOverride(event.getGuild().getPublicRole()
                                                         .getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                         .complete();

        var team = jam.teams()
                .create(teamName);
        var meta = team.meta();
        meta.leader(event.getMember().getIdLong());
        meta.textChannel(text.getIdLong());
        meta.voiceChannel(voice.getIdLong());
        meta.role(role.getIdLong());

        event.getGuild().addRoleToMember(event.getMember(), role).queue();
        event.getHook().editOriginal(context.localize("command.team.create.message.created")).queue();
    }
}
