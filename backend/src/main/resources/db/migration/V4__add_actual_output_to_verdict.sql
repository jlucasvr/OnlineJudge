-- =============================================================================
-- V4: Adiciona coluna actual_output na tabela verdict
--     Armazena a saída gerada pelo código do usuário (truncada se necessária).
--     Só é preenchida quando o test_case tem is_sample = true.
-- =============================================================================

ALTER TABLE verdict
    ADD COLUMN actual_output TEXT;
