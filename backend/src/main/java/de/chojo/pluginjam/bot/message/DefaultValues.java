package de.chojo.pluginjam.bot.message;

import de.chojo.pluginjam.database.entity.team.Team;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class DefaultValues {

    public static String getTeamProjectDescription(Team team, MessageResolver resolver, DiscordLocale locale) {
        if (team.meta().getProjectDescription() == null || team.meta().getProjectDescription().isBlank()) {
            return resolver.resolve("word-no-description", locale);
        }
        return team.meta().getProjectDescription();
    }

    public static String getTeamProjectUrl(Team team, MessageResolver resolver, DiscordLocale locale) {
        if (team.meta().getProjectDescription() == null || team.meta().getProjectDescription().isBlank()) {
            return resolver.resolve("word-no-url", locale);
        }
        return team.meta().getProjectUrl();
    }
}
