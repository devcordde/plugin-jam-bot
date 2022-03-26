CREATE TABLE IF NOT EXISTS gamejam.jam
(
    id       SERIAL,
    guild_id BIGINT NOT NULL,
    CONSTRAINT jam_pk
        PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS jam_guild_id_index
    ON gamejam.jam (guild_id);

CREATE TABLE IF NOT EXISTS gamejam.jam_time
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

CREATE TABLE IF NOT EXISTS gamejam.jam_meta
(
    jam_id INTEGER NOT NULL,
    topic  TEXT    NOT NULL,
    CONSTRAINT jam_topic_pk
        PRIMARY KEY (jam_id),
    CONSTRAINT jam_topic_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gamejam.team
(
    id     SERIAL,
    jam_id INTEGER NOT NULL,
    CONSTRAINT team_pk
        PRIMARY KEY (id),
    CONSTRAINT team_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gamejam.team_member
(
    team_id INTEGER NOT NULL,
    user_id BIGINT,
    CONSTRAINT team_member_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS team_member_team_id_user_id_uindex
    ON gamejam.team_member (team_id, user_id);

CREATE INDEX IF NOT EXISTS team_member_team_id_index
    ON gamejam.team_member (team_id);

CREATE TABLE IF NOT EXISTS gamejam.team_meta
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

CREATE TABLE IF NOT EXISTS gamejam.vote
(
    team_id  BIGINT            NOT NULL,
    voter_id BIGINT            NOT NULL,
    points   INTEGER DEFAULT 0 NOT NULL,
    CONSTRAINT vote_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gamejam.jam_registrations
(
    jam_id  INTEGER NOT NULL,
    user_id BIGINT  NOT NULL,
    CONSTRAINT jam_registrations_jam_id_fk
        FOREIGN KEY (jam_id) REFERENCES gamejam.jam
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS jam_registrations_jam_id_user_id_uindex
    ON gamejam.jam_registrations (jam_id, user_id);

CREATE TABLE IF NOT EXISTS gamejam.jam_settings
(
    guild_id  BIGINT            NOT NULL,
    jam_role  BIGINT  DEFAULT 0 NOT NULL,
    team_size INTEGER DEFAULT 4 NOT NULL,
    CONSTRAINT jam_settings_pk
        PRIMARY KEY (guild_id)
);

CREATE TABLE IF NOT EXISTS gamejam.jam_state
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

CREATE TABLE IF NOT EXISTS gamejam.version
(
    major INTEGER,
    patch INTEGER
);

CREATE TABLE IF NOT EXISTS gamejam.settings
(
    guild_id     BIGINT,
    manager_role BIGINT,
    locale       INTEGER
);
