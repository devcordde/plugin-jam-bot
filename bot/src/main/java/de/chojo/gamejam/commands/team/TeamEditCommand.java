/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;

import java.util.List;

@Bundle("locale")
@Interaction
public class TeamEditCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamEditCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team edit")
    public void onCommand(CommandEvent event) {
        var optJam = commandContextProvider.guilds().guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var optTeam = optJam.get().teams().byMember(event.getMember());
        if (optTeam.isEmpty()) {
            event.with().ephemeral(true).reply("error.noteam");
            return;
        }

        var meta = optTeam.get().meta();

        event.replyModal("onModal", List.of(
                        Label.of("Project Description", TextInput.create("descr", TextInputStyle.PARAGRAPH)
                                .setValue(meta.projectDescription().isBlank() ? "None" : meta.projectDescription())
                                .setMaxLength(100)
                                .build()),
                        Label.of("Project url", TextInput.create("url", TextInputStyle.SHORT)
                                .setValue(meta.projectUrl().isBlank() ? "none" : meta.projectUrl())
                                .setMaxLength(200)
                                .build())
                )
        );
    }

    @Modal("Edit Profile")
    public void onModal(ModalEvent event) {
        var optJam = commandContextProvider.guilds().guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            return;
        }
        var optTeam = optJam.get().teams().byMember(event.getMember());
        if (optTeam.isEmpty()) {
            return;
        }

        var meta = optTeam.get().meta();
        meta.projectDescription(event.value("descr").getAsString());
        meta.projectUrl(event.value("url").getAsString());
        event.reply("Updated");
    }
}
