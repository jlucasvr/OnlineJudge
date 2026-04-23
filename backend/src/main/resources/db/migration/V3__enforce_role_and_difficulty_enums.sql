-- =============================================================================
-- V3: Padroniza role (user) e difficulty (problem) como enums conhecidos
-- =============================================================================

-- -----------------------------------------------------------------------------
-- PARTE 1 — Tabela "user": coluna role
-- -----------------------------------------------------------------------------

-- 1.1 Normaliza valores legados para MAIÚSCULO e adiciona prefixo ROLE_ se necessário
UPDATE "user"
SET role = UPPER(role)
WHERE role <> UPPER(role);

UPDATE "user"
SET role = 'ROLE_' || role
WHERE role NOT LIKE 'ROLE_%';

-- 1.2 Corrige valores alternativos que possam existir de versões anteriores
UPDATE "user" SET role = 'ROLE_USER'  WHERE role IN ('ROLE_CONTESTANT', 'ROLE_MEMBER');
UPDATE "user" SET role = 'ROLE_ADMIN' WHERE role IN ('ROLE_ADMINISTRATOR', 'ROLE_SUPERUSER');

-- 1.3 Altera o default para o novo padrão
ALTER TABLE "user"
    ALTER COLUMN role SET DEFAULT 'ROLE_USER';

-- 1.4 Remove constraint anterior (se existir) e cria a nova
ALTER TABLE "user"
    DROP CONSTRAINT IF EXISTS chk_user_role;

ALTER TABLE "user"
    ADD CONSTRAINT chk_user_role
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'));

-- -----------------------------------------------------------------------------
-- PARTE 2 — Tabela problem: coluna difficulty
-- -----------------------------------------------------------------------------

-- 2.1 Normaliza valores legados para MAIÚSCULO
UPDATE problem
SET difficulty = UPPER(difficulty)
WHERE difficulty <> UPPER(difficulty);

-- 2.2 Corrige possíveis variações em português ou inglês informal
UPDATE problem SET difficulty = 'EASY'   WHERE difficulty IN ('FACIL', 'FÁCIL', 'EASY', 'LOW', '1');
UPDATE problem SET difficulty = 'MEDIUM' WHERE difficulty IN ('MEDIO', 'MÉDIO', 'MEDIUM', 'MID', 'NORMAL', '2');
UPDATE problem SET difficulty = 'HARD'   WHERE difficulty IN ('DIFICIL', 'DIFÍCIL', 'HARD', 'HIGH', '3');

-- 2.3 Remove constraint anterior (se existir) e cria a nova
ALTER TABLE problem
    DROP CONSTRAINT IF EXISTS chk_problem_difficulty;

ALTER TABLE problem
    ADD CONSTRAINT chk_problem_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'));
