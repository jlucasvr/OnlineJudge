-- Ativar extensão para UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================
-- TABELA: user
-- =========================
CREATE TABLE "user" (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        username VARCHAR(50) NOT NULL UNIQUE,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'contestant',
                        created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================
-- TABELA: problem
-- =========================
CREATE TABLE problem (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         title VARCHAR(255) NOT NULL,
                         statement TEXT NOT NULL,
                         difficulty VARCHAR(20) NOT NULL,
                         time_limit_ms INT NOT NULL,
                         memory_limit_kb INT NOT NULL,
                         is_public BOOLEAN NOT NULL DEFAULT FALSE,
                         author_id UUID,
                         created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                         CONSTRAINT fk_problem_author FOREIGN KEY (author_id) REFERENCES "user"(id)
);

-- =========================
-- TABELA: test_case
-- =========================
CREATE TABLE test_case (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           problem_id UUID NOT NULL,
                           order_index INT NOT NULL,
                           input_path VARCHAR(500) NOT NULL,
                           output_path VARCHAR(500) NOT NULL,
                           points INT NOT NULL DEFAULT 0,
                           is_sample BOOLEAN NOT NULL DEFAULT FALSE,
                           CONSTRAINT fk_testcase_problem FOREIGN KEY (problem_id) REFERENCES problem(id)
);

-- =========================
-- TABELA: tag
-- =========================
CREATE TABLE tag (
                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                     name VARCHAR(100) NOT NULL UNIQUE
);

-- =========================
-- TABELA: problem_tag (N:N)
-- =========================
CREATE TABLE problem_tag (
                             problem_id UUID NOT NULL,
                             tag_id UUID NOT NULL,
                             PRIMARY KEY (problem_id, tag_id),
                             CONSTRAINT fk_problem_tag_problem FOREIGN KEY (problem_id) REFERENCES problem(id),
                             CONSTRAINT fk_problem_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
);

-- =========================
-- TABELA: submission
-- =========================
CREATE TABLE submission (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL,
                            problem_id UUID NOT NULL,
                            language VARCHAR(30) NOT NULL,
                            code_path VARCHAR(500) NOT NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'pending',
                            execution_time_ms INT,
                            memory_used_kb INT,
                            submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            CONSTRAINT fk_submission_user FOREIGN KEY (user_id) REFERENCES "user"(id),
                            CONSTRAINT fk_submission_problem FOREIGN KEY (problem_id) REFERENCES problem(id)
);

-- =========================
-- TABELA: verdict
-- =========================
CREATE TABLE verdict (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         submission_id UUID NOT NULL,
                         test_case_id UUID NOT NULL,
                         result VARCHAR(10) NOT NULL,
                         execution_time_ms INT,
                         memory_used_kb INT,
                         checker_output TEXT,
                         CONSTRAINT fk_verdict_submission FOREIGN KEY (submission_id) REFERENCES submission(id),
                         CONSTRAINT fk_verdict_testcase FOREIGN KEY (test_case_id) REFERENCES test_case(id)
);

-- =========================
-- ÍNDICES (PERFORMANCE)
-- =========================

-- Submission
CREATE INDEX idx_submission_user ON submission(user_id);
CREATE INDEX idx_submission_problem ON submission(problem_id);

-- Verdict
CREATE INDEX idx_verdict_submission ON verdict(submission_id);
CREATE INDEX idx_verdict_testcase ON verdict(test_case_id);

-- Test case
CREATE INDEX idx_testcase_problem ON test_case(problem_id);

-- Problem
CREATE INDEX idx_problem_author ON problem(author_id);