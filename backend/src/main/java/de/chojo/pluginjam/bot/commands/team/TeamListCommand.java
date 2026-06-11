    /*
     *     SPDX-License-Identifier: AGPL-3.0-only
     *
     *     Copyright (C) 2022 DevCord Team and Contributor
     */

    package de.chojo.pluginjam.bot.commands.team;

    import com.google.inject.Inject;
    import de.chojo.pluginjam.bot.commands.CommandContextProvider;
    import de.chojo.pluginjam.bot.message.DefaultValues;
    import de.chojo.pluginjam.bot.util.MentionUtil;
    import de.chojo.pluginjam.database.entity.team.Team;
    import io.github.kaktushose.jdac.annotations.i18n.Bundle;
    import io.github.kaktushose.jdac.annotations.interactions.Button;
    import io.github.kaktushose.jdac.annotations.interactions.Command;
    import io.github.kaktushose.jdac.annotations.interactions.Interaction;
    import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
    import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
    import io.github.kaktushose.jdac.dispatching.reply.Component;
    import io.github.kaktushose.jdac.message.resolver.MessageResolver;
    import net.dv8tion.jda.api.components.actionrow.ActionRow;
    import net.dv8tion.jda.api.components.buttons.ButtonStyle;
    import net.dv8tion.jda.api.components.container.Container;
    import net.dv8tion.jda.api.components.container.ContainerChildComponent;
    import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
    import net.dv8tion.jda.api.interactions.DiscordLocale;

    import java.util.List;
    import java.util.UUID;
    import java.util.stream.Collectors;

    @Bundle("locale")
    @Interaction
    public class TeamListCommand {
        private final CommandContextProvider commandContextProvider;
        private final MessageResolver messageResolver;

        private List<Team> pageEntries;
        private int currentPage;

        @Inject
        public TeamListCommand(CommandContextProvider commandContextProvider, MessageResolver messageResolver) {
            this.commandContextProvider = commandContextProvider;
            this.messageResolver = messageResolver;
        }

        @Command(value = "team list")
        public void onCommand(CommandEvent event) {
            var guildId = event.getGuild().getIdLong();
            var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);

            if (optJam.isEmpty()) {
                event.with().ephemeral(true).reply("error-nojamactive");
                return;
            }
            var jam = optJam.get();

            pageEntries = commandContextProvider.teamService().getTeamsByJamId(jam.id());

            currentPage = 0;
            event.with().ephemeral(true).reply(renderPage(0, pageEntries, event.getUserLocale()));
        }

        private Container renderPage(int page, List<Team> entries, DiscordLocale locale) {
            int pageSize = 1;

            boolean hasNext = page < (entries.size() - 1) / pageSize;
            boolean hasPrev = page > 0;

            int start = page * pageSize;
            int end = Math.min(start + pageSize, entries.size());

            List<ContainerChildComponent> teamContents = entries.subList(start, end).stream().map(team -> {
                var membersContent = team.members()
                        .stream()
                        .map(teamMember -> String.format("- %s", MentionUtil.user(teamMember.userId())))
                        .collect(Collectors.joining(" \n"));

                var contentString = """
                        # %s (%s)
                        ### %s:
                        *%s*
                        ### %s:
                        *%s*
                        ### %s:
                        *%s*
                        """.formatted(
                        team.meta().getTeamName(),
                        MentionUtil.role(team.meta().getRoleId()),
                        messageResolver.resolve("word-description", locale),
                        DefaultValues.getTeamProjectDescription(team, messageResolver, locale),
                        messageResolver.resolve("word-project-url", locale),
                        DefaultValues.getTeamProjectUrl(team, messageResolver, locale),
                        messageResolver.resolve("word-members", locale),
                        membersContent);

                return TextDisplay.of(contentString);
            }).collect(Collectors.toList());

            int maxPages = (int) Math.ceil(entries.size() / (double) pageSize);

            teamContents.add(
                    ActionRow.of(
                            hasPrev ? Component.enabled("onPrev") : Component.disabled("onPrev"),
                            net.dv8tion.jda.api.components.buttons.Button.of(ButtonStyle.SECONDARY, UUID.randomUUID().toString(), "%d / %d".formatted(currentPage + 1, maxPages), null),
                            hasNext ? Component.enabled("onNext") : Component.disabled("onNext")
                    )
            );

            return Container.of(teamContents);
        }

        @Button(value = "◀", style = ButtonStyle.SECONDARY)
        public void onPrev(ComponentEvent event) {
            currentPage--;
            event.with().keepComponents(false).ephemeral(true).reply(renderPage(currentPage, pageEntries, event.getUserLocale()));
        }

        @Button(value = "▶", style = ButtonStyle.SECONDARY)
        public void onNext(ComponentEvent event) {
            currentPage++;
            event.with().keepComponents(false).ephemeral(true).reply(renderPage(currentPage, pageEntries, event.getUserLocale()));
        }
    }
