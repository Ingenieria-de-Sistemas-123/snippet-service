
CREATE TABLE IF NOT EXISTS snippets (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    language VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    asset_key VARCHAR(300) NOT NULL UNIQUE,
    owner_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );
