package br.lab.testesubmissao.Dto.verdict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload enviado pelo Worker para registrar o resultado de um caso de teste.
 * Chamado via POST /internal/verdicts
 */
public record CreateVerdictRequest(
        @NotNull UUID submissionId,
        @NotNull UUID testCaseId,
        @NotBlank String result,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        String checkerOutput,
        /** Só será salvo se o test case tiver is_sample = true */
        String actualOutput
) {}
