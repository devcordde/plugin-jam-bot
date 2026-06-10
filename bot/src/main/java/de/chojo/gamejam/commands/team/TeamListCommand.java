/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.embeds.Embed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;

@Bundle("locale")
@Interaction
public class TeamListCommand {
    private final CommandContextProvider commandContextProvider;

    private List<Embed> pages;
    private int currentPage;

    @Inject
    public TeamListCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team list")
    public void onCommand(CommandEvent event) {
        JamGuild guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        pages = jam.teams().teams().stream().map(team -> team.profileEmbed(event)).toList();
        currentPage = 0;
        event.with().components(Component.disabled("onPrev"), Component.enabled("onNext"))
                .embeds(pages.getFirst()).reply();
    }

    //TODO: localization
    @Button(value = "◀ Zurück", style = ButtonStyle.SECONDARY)
    public void onPrev(ComponentEvent event) {
        currentPage--;
        event.with()
                .components(
                        currentPage == 0 ? Component.disabled("onPrev") : Component.enabled("onPrev"),
                        Component.enabled("onNext")
                )
                .embeds(pages.get(currentPage)).reply();
    }

    //TODO: localization
    @Button(value = "Weiter ▶", style = ButtonStyle.SECONDARY)
    public void onNext(ComponentEvent event) {
        currentPage++;
        event.with()
                .components(
                        Component.enabled("onPrev"),
                        currentPage == pages.size() - 1 ? Component.disabled("onNext") : Component.enabled("onNext")
                )
                .embeds(pages.get(currentPage)).reply();
    }
}
