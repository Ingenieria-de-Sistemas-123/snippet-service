UPDATE snippets
SET version = NULL
WHERE version IS NOT NULL AND LOWER(TRIM(version)) = 'unspecified';
