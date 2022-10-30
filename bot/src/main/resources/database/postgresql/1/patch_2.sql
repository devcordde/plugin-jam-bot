ALTER TABLE gamejam.team_meta
    ALTER COLUMN leader_id SET DEFAULT 0;

ALTER TABLE gamejam.team_meta
    ALTER COLUMN role_id SET DEFAULT 0;

ALTER TABLE gamejam.team_meta
    ALTER COLUMN text_channel_id SET DEFAULT 0;

ALTER TABLE gamejam.team_meta
    ALTER COLUMN voice_channel_id SET DEFAULT 0;

ALTER TABLE gamejam.team_meta
    RENAME COLUMN name TO team_name;

ALTER TABLE gamejam.team_meta
    ADD project_description TEXT DEFAULT '' NOT NULL;

ALTER TABLE gamejam.team_meta
    ADD project_url TEXT DEFAULT '' NOT NULL;
