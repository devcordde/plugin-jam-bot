CREATE SEQUENCE gamejam.jam_times_id_seq
    AS INTEGER;

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
    jam_id             INTEGER   NOT NULL,
    registration_start TIMESTAMP NOT NULL,
    registration_end   TIMESTAMP NOT NULL,
    zone_id            TEXT      NOT NULL,
    jam_start          TIMESTAMP NOT NULL,
    jam_end            TIMESTAMP NOT NULL,
    CONSTRAINT jam_times_pk
        PRIMARY KEY (jam_id),
    CONSTRAINT jam_times_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

ALTER SEQUENCE gamejam.jam_times_id_seq OWNED BY gamejam.jam_time.jam_id;

CREATE TABLE gamejam.jam_meta
(
    jam_id INTEGER NOT NULL,
    topic  TEXT    NOT NULL,
    CONSTRAINT jam_topic_pk
        PRIMARY KEY (jam_id),
    CONSTRAINT jam_topic_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
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

CREATE INDEX team_member_team_id_index
    ON gamejam.team_member (team_id);

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
            ON DELETE CASCADE
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

CREATE TABLE gamejam.jam_registrations
(
    jam_id  INTEGER NOT NULL,
    user_id BIGINT  NOT NULL,
    CONSTRAINT jam_registrations_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX jam_registrations_jam_id_user_id_uindex
    ON gamejam.jam_registrations (jam_id, user_id);

CREATE TABLE gamejam.jam_settings
(
    guild_id     BIGINT            NOT NULL,
    jam_role     BIGINT  DEFAULT 0 NOT NULL,
    team_size    INTEGER DEFAULT 4 NOT NULL,
    manager_role BIGINT,
    CONSTRAINT jam_settings_pk
        PRIMARY KEY (guild_id)
);

CREATE TABLE gamejam.jam_state
(
    jam_id INTEGER               NOT NULL,
    active BOOLEAN DEFAULT FALSE NOT NULL,
    voting BOOLEAN DEFAULT FALSE NOT NULL,
    ended  BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT jam_state_pk
        PRIMARY KEY (jam_id),
    CONSTRAINT jam_state_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);
