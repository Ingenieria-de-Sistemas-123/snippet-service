CREATE TABLE snippet (
                         id UUID PRIMARY KEY,
                         name TEXT NOT NULL,
                         language TEXT NOT NULL,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
