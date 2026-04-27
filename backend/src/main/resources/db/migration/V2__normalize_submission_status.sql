-- Normalize legacy submission statuses and enforce allowed values.

UPDATE submission
SET status = UPPER(status)
WHERE status <> UPPER(status);

UPDATE submission SET status = 'TIME_LIMIT' WHERE status = 'TIME_LIMIT_EXCEEDED';
UPDATE submission SET status = 'MEMORY_LIMIT' WHERE status = 'MEMORY_LIMIT_EXCEEDED';

ALTER TABLE submission
    ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE submission
    DROP CONSTRAINT IF EXISTS chk_submission_status;

ALTER TABLE submission
    ADD CONSTRAINT chk_submission_status
        CHECK (status IN (
            'PENDING',
            'QUEUED',
            'RUNNING',
            'ACCEPTED',
            'WRONG_ANSWER',
            'TIME_LIMIT',
            'MEMORY_LIMIT',
            'RUNTIME_ERROR',
            'COMPILATION_ERROR',
            'INTERNAL_ERROR'
        ));
