CREATE TABLE IF NOT EXISTS snippet_tests (
    id UUID PRIMARY KEY,
    snippet_id UUID NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    script TEXT NOT NULL,
    last_run_at TIMESTAMPTZ,
    last_run_exit_code INTEGER,
    last_run_output TEXT,
    last_run_error TEXT
);
