CREATE UNIQUE INDEX vote_team_id_voter_id_uindex
    ON gamejam.vote (team_id, voter_id);
