-- =============================================================================
-- Cria o usuário de serviço do Worker (executar após o primeiro boot da API)
--
-- O Worker precisa de um usuário com ROLE_ADMIN para acessar /internal/*
-- Informe o hash via psql, sem gravar senha ou hash real no repositório:
--   psql "$DATABASE_URL" -v WORKER_PASSWORD_HASH='bcrypt-gerado' -f worker_service_account.sql
-- =============================================================================

INSERT INTO "user" (id, username, email, password_hash, role, created_at)
VALUES (
    gen_random_uuid(),
    'worker',
    'worker@internal.oj',
    :'WORKER_PASSWORD_HASH',
    'ROLE_ADMIN',
    NOW()
)
ON CONFLICT (username) DO NOTHING;
