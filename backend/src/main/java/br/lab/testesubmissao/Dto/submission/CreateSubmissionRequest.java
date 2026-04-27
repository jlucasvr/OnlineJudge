package br.lab.testesubmissao.Dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSubmissionRequest(
        @NotNull(message = "problemId é obrigatório")
        UUID problemId,

        @NotBlank(message = "language é obrigatório")
        @Pattern(regexp = "^(c|cpp|java|python)$", message = "language deve ser c, cpp, java ou python")
        String language,

        @NotBlank(message = "sourceCode é obrigatório")
        @Size(max = 100000, message = "sourceCode excede o limite de 100000 caracteres")
        String sourceCode
) {
}
