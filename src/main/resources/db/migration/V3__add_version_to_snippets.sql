ALTER TABLE snippets
    ADD COLUMN version VARCHAR(20) NOT NULL DEFAULT 'unspecified';

ALTER TABLE snippets
    ALTER COLUMN version DROP DEFAULT;
