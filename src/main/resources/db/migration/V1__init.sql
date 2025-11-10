CREATE TABLE snippet
(
    id         UUID NOT NULL,
    name       VARCHAR(255),
    description VARCHAR(500),
    language   VARCHAR(255),
    version    VARCHAR(50),
    content    TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_snippet PRIMARY KEY (id)
);