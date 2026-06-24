CREATE SEQUENCE gamejam.jam_times_id_seq
    AS INTEGER;

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

ALTER SEQUENCE gamejam.jam_times_id_seq OWNED BY gamejam.jam_time.jam_id;

CREATE TABLE IF NOT EXISTS gamejam.jam_meta
(
    jam_id  INTEGER NOT NULL,
    topic   TEXT    NOT NULL,
    tagline TEXT    NOT NULL,
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
    team_id             INTEGER           NOT NULL,
    team_name           TEXT              NOT NULL,
    leader_id           BIGINT DEFAULT 0  NOT NULL,
    role_id             BIGINT DEFAULT 0  NOT NULL,
    text_channel_id     BIGINT DEFAULT 0  NOT NULL,
    voice_channel_id    BIGINT DEFAULT 0  NOT NULL,
    project_description TEXT   DEFAULT '' NOT NULL,
    project_url         TEXT   DEFAULT '' NOT NULL,
    token               TEXT,
    CONSTRAINT team_meta_pk
        PRIMARY KEY (team_id),
    CONSTRAINT team_meta_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gamejam.votes
(
    team_id  BIGINT            NOT NULL,
    voter_id BIGINT            NOT NULL,
    points   INTEGER DEFAULT 0 NOT NULL,
    CONSTRAINT vote_team_id_fk
        FOREIGN KEY (team_id) REFERENCES gamejam.team
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS vote_team_id_voter_id_uindex
    ON gamejam.votes (team_id, voter_id);

CREATE OR REPLACE VIEW gamejam.team_ranking AS
SELECT ROW_NUMBER() OVER (PARTITION BY jam_id ORDER BY points DESC ) as rank, team_id, points, jam_id
FROM (SELECT team_id, SUM(points) AS points
      FROM gamejam.votes
      GROUP BY team_id) a
         LEFT JOIN gamejam.team t ON t.id = a.team_id
ORDER BY points DESC;

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

CREATE TABLE IF NOT EXISTS gamejam.settings
(
    guild_id         BIGINT  DEFAULT 0 NOT NULL,
    manager_role     BIGINT  DEFAULT 0,
    participant_role BIGINT  DEFAULT 0 NOT NULL,
    team_size        INTEGER DEFAULT 4 NOT NULL,
    locale           TEXT,
    CONSTRAINT settings_pk
        PRIMARY KEY (guild_id)
);
