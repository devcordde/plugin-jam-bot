package de.chojo.pluginjam.service;

import de.chojo.pluginjam.database.entity.VotingRank;
import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.database.repository.VotesRankingRepository;
import de.chojo.pluginjam.database.repository.VotesRepository;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

@Singleton
public class VoteService {
    private final VotesRepository votesRepository;
    private final VotesRankingRepository votesRankingRepository;

    public VoteService(VotesRepository votesRepository, VotesRankingRepository votesRankingRepository) {
        this.votesRepository = votesRepository;
        this.votesRankingRepository = votesRankingRepository;
    }

    public int getGivenPointsByUser(User user) {
        return votesRepository.findByVoterId(user.getIdLong());
    }

    public int getPointsByTeam(Team team) {
        return votesRepository.findByTeamId(team.id());
    }

    public void voteForTeam(User user, Team team, int points) {
        votesRepository.save(team.id(), user.getIdLong(), points);
    }

    public Optional<VotingRank> getVotingRank(int teamId) {
        return votesRankingRepository.findById(teamId);
    }

    public List<VotingRank> getVotingRanks(int jamId) {
        return votesRankingRepository.findAllByJamId(jamId);
    }
}
