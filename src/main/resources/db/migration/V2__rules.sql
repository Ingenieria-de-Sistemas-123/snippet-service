CREATE TABLE IF NOT EXISTS rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL,
    value INTEGER,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO rules (id, name, type, is_active, value) VALUES
('1', 'indentation', 'FORMAT', TRUE, 3),
('2', 'open-if-block-on-same-line', 'FORMAT', FALSE, NULL),
('3', 'max-line-length', 'FORMAT', TRUE, 100),
('4', 'no-trailing-spaces', 'FORMAT', FALSE, NULL),
('5', 'no-multiple-empty-lines', 'FORMAT', FALSE, NULL),
('1-lint', 'no-expressions-in-print-line', 'LINT', TRUE, NULL),
('2-lint', 'no-unused-vars', 'LINT', TRUE, NULL),
('3-lint', 'no-undef-vars', 'LINT', FALSE, NULL),
('4-lint', 'no-unused-params', 'LINT', FALSE, NULL)
ON CONFLICT (id) DO NOTHING;
