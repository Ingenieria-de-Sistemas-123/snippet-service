ALTER TABLE snippets
    ADD COLUMN compliance_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN compliance_message VARCHAR(500);

UPDATE snippets
SET compliance_status = 'VALID'
WHERE compliance_status = 'UNKNOWN';

ALTER TABLE snippets
    ALTER COLUMN compliance_status DROP DEFAULT;
