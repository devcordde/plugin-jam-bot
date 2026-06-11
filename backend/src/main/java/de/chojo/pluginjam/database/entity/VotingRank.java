package de.chojo.pluginjam.database.entity;

import de.chojo.pluginjam.database.entity.team.Team;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;

import java.util.List;

@MappedEntity(value = "team_ranking", schema = "gamejam")
public record VotingRank(
        @Id
        @MappedProperty("team_id")
        Long teamId,
        Integer rank,
        Integer points,
        @MappedProperty("jam_id")
        Integer jamId,
        @Relation(value = Relation.Kind.ONE_TO_ONE, mappedBy = "id")
        Team team
) {

}
