package br.lab.testesubmissao.Dto.submission;

import br.lab.testesubmissao.Entity.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateSubmissionStatusRequest(
        @NotNull(message = "status é obrigatório")
        SubmissionStatus status,

        @PositiveOrZero(message = "executionTimeMs deve ser >= 0")
        Integer executionTimeMs,

        @PositiveOrZero(message = "memoryUsedKb deve ser >= 0")
        Integer memoryUsedKb
) {
}
