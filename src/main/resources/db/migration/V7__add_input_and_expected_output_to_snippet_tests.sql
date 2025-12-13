ALTER TABLE snippet_tests
    ADD COLUMN input TEXT,
    ADD COLUMN expected_output TEXT NOT NULL DEFAULT '';

-- script deja de ser obligatorio; se mantiene por compatibilidad pero JPA ya no lo usa
ALTER TABLE snippet_tests
    ALTER COLUMN script DROP NOT NULL;
