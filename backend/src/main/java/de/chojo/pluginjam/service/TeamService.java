package de.chojo.pluginjam.service;

import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.database.entity.team.TeamMember;
import de.chojo.pluginjam.database.entity.team.TeamMeta;
import de.chojo.pluginjam.database.repository.TeamMemberRepository;
import de.chojo.pluginjam.database.repository.TeamMetaRepository;
import de.chojo.pluginjam.database.repository.TeamRepository;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

@Singleton
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMetaRepository teamMetaRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, TeamMetaRepository teamMetaRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamMetaRepository = teamMetaRepository;
    }

    public Team createTeam(long jamId, String name, long leaderId, long roleId, long textChannelId, long voiceChannelId) {
        var team = teamRepository.save(new Team(null, (int) jamId, null, List.of()));
        teamMetaRepository.save(new TeamMeta(team.id(), name, leaderId, roleId, textChannelId, voiceChannelId, "", "", ""));
        return teamRepository.findById(team.id()).orElseThrow();
    }

    public Optional<Team> getTeam(int id) {
        return teamRepository.findById(id);
    }

    public Optional<Team> getUserTeam(long userId) {
        return teamRepository.findByMembersUserId(userId);
    }

    public void removeUserFromTeam(long userId) {
        getUserTeam(userId).ifPresent(team -> teamMemberRepository.deleteByTeamIdAndUserId(team.id(), userId));
    }

    public Optional<Team> getTeamByName(int jamId, String teamName) {
        return teamRepository.findByJamId(jamId).stream()
                .filter(team -> team.meta() != null && teamName.equals(team.meta().teamName()))
                .findFirst();
    }

    public void joinTeam(Team team, Member user, Guild guild) {
        teamMemberRepository.save(new TeamMember(team.id(), user.getIdLong()));
        guild.addRoleToMember(user, guild.getRoleById(team.meta().roleId())).queue();
    }

    public void leaveTeam(Team team, Member user, Guild guild) {
        teamMemberRepository.deleteByTeamIdAndUserId(team.id(), user.getIdLong());
        guild.removeRoleFromMember(user, guild.getRoleById(team.meta().roleId())).queue();
    }

    public void disbandTeam(Team team, Guild guild) {
        teamMemberRepository.deleteAllByTeamId(team.id());
        teamRepository.deleteById(team.id());
        teamMetaRepository.deleteById(team.id());

        guild.getRoleById(team.meta().roleId()).delete().queue();
        guild.getTextChannelById(team.meta().textChannelId()).delete().queue();
        guild.getVoiceChannelById(team.meta().voiceChannelId()).delete().queue();
    }
}
