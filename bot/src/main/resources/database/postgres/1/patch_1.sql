CREATE UNIQUE INDEX vote_team_id_voter_id_uindex
    ON gamejam.vote (team_id, voter_id);

CREATE OR REPLACE VIEW gamejam.team_ranking AS
SELECT ROW_NUMBER() OVER (PARTITION BY jam_id ORDER BY points DESC ) as rank, team_id, points, jam_id
FROM (SELECT team_id, SUM(points) AS points
      FROM gamejam.vote
      GROUP BY team_id) a
         LEFT JOIN gamejam.team t ON t.id = a.team_id
ORDER BY points DESC
