CREATE TABLE gamejam.jam
(
    id       SERIAL,
    guild_id BIGINT NOT NULL,
    CONSTRAINT jam_pk
        PRIMARY KEY (id)
);

CREATE INDEX jam_guild_id_index
    ON gamejam.jam (guild_id);

CREATE TABLE gamejam.jam_time
(
    id                 INTEGER   NOT NULL,
    registration_start TIMESTAMP NOT NULL,
    registration_end   TIMESTAMP NOT NULL,
    start_time         TIMESTAMP NOT NULL,
    vote_start         TIMESTAMP NOT NULL,
    vote_end           TIMESTAMP NOT NULL,
    end_time           TIMESTAMP NOT NULL,
    CONSTRAINT jam_times_pk
        PRIMARY KEY (id),
    CONSTRAINT jam_times_jam_id_fk
        FOREIGN KEY (id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE TABLE gamejam.jam_topic
(
    id    INTEGER NOT NULL,
    topic TEXT    NOT NULL,
    CONSTRAINT jam_topic_pk
        PRIMARY KEY (id),
    CONSTRAINT jam_topic_jam_id_fk
        FOREIGN KEY (id) REFERENCES gamejam.jam
);

CREATE TABLE gamejam.team
(
    id     SERIAL,
    jam_id INTEGER NOT NULL,
    CONSTRAINT team_pk
        PRIMARY KEY (id),
    CONSTRAINT team_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE TABLE gamejam.team_member
(
    team_id INTEGER NOT NULL,
    user_id BIGINT
);

CREATE UNIQUE INDEX team_member_team_id_user_id_uindex
    ON gamejam.team_member (team_id, user_id);

CREATE TABLE gamejam.team_meta
(
    team_id          INTEGER NOT NULL,
    name             TEXT    NOT NULL,
    leader_id        BIGINT  NOT NULL,
    role_id          BIGINT  NOT NULL,
    text_channel_id  BIGINT  NOT NULL,
    voice_channel_id BIGINT  NOT NULL,
    CONSTRAINT team_meta_pk
        PRIMARY KEY (team_id),
    CONSTRAINT team_meta_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
);

CREATE TABLE gamejam.vote
(
    jam_id   INTEGER           NOT NULL,
    team_id  BIGINT            NOT NULL,
    voter_id BIGINT            NOT NULL,
    points   INTEGER DEFAULT 0 NOT NULL,
    CONSTRAINT vote_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE,
    CONSTRAINT vote_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX vote_jam_id_team_id_voter_id_uindex
    ON gamejam.vote (jam_id, team_id, voter_id);
