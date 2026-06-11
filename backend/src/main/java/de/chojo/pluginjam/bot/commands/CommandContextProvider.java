package de.chojo.pluginjam.bot.commands;

import de.chojo.pluginjam.service.JamService;
import de.chojo.pluginjam.service.SettingsService;
import de.chojo.pluginjam.service.TeamService;
import de.chojo.pluginjam.service.VoteService;

public class CommandContextProvider {
    private final JamService jamService;
    private final SettingsService settingsService;
    private final TeamService teamService;
    private final VoteService voteService;

    public CommandContextProvider(JamService jamService, SettingsService settingsService, TeamService teamService, VoteService voteService) {
        this.jamService = jamService;
        this.settingsService = settingsService;
        this.teamService = teamService;
        this.voteService = voteService;
    }

    public JamService pluginJamService() {
        return jamService;
    }

    public SettingsService settingsService() {
        return settingsService;
    }

    public TeamService teamService() {
        return teamService;
    }

    public VoteService voteService() {
        return voteService;
    }
}
