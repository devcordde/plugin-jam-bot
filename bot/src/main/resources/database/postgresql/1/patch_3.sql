ALTER TABLE public.team_meta
    ADD COLUMN IF NOT EXISTS token TEXT;
