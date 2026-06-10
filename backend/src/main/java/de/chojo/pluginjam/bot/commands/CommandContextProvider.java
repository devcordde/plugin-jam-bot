package de.chojo.pluginjam.bot.commands;

import de.chojo.pluginjam.service.JamService;
import de.chojo.pluginjam.service.SettingsService;
import de.chojo.pluginjam.service.TeamService;

public class CommandContextProvider {
    private final JamService jamService;
    private final SettingsService settingsService;
    private final TeamService teamService;

    public CommandContextProvider(JamService jamService, SettingsService settingsService, TeamService teamService) {
        this.jamService = jamService;
        this.settingsService = settingsService;
        this.teamService = teamService;
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
}
