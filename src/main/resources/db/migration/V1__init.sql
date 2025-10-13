CREATE TABLE snippet
(
    id         UUID NOT NULL,
    name       VARCHAR(255),
    language   VARCHAR(255),
    content    TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_snippet PRIMARY KEY (id)
);