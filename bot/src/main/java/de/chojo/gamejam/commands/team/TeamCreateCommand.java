/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;


import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.gamejam.util.Token;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.Collections;
import java.util.EnumSet;

@Bundle("locale")
@Interaction
public final class TeamCreateCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamCreateCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }
    
    @Command(value = "team create")
    public void onCommand(CommandEvent event, @Param("name") String teamName) {
        var jamGuild = commandContextProvider.guilds().guild(event.getGuild());
        var optJam = jamGuild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.with().ephemeral(true).reply("error.votingactive");
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.with().ephemeral(true).reply("command.team.create.message.unregistered");
            return;
        }
        var jamUser = jam.user(event.getMember());

        var userTeam = jamUser.team();
        if (userTeam.isPresent()) {
            event.with().ephemeral(true).reply("command.team.create.message.alreadymember");
            return;
        }

        // TODO: Enforce constrains of length and allowed chars

        var optTeam = jam.teams().byName(teamName);

        if (optTeam.isPresent()) {
            event.with().ephemeral(true).reply("command.team.create.message.nametaken");
            return;
        }

        event.deferReply(true);

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
        meta.leader(event.getMember());
        meta.textChannel(text);
        meta.voiceChannel(voice);
        meta.role(role);
        meta.token(Token.generate(40));
        jamUser.join(team);

        event.with().ephemeral(true).reply("command.team.create.message.created");
    }
}
