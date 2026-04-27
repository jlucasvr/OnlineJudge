package br.lab.testesubmissao.Dto.testcase;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTestCaseRequest(

        @NotNull(message = "orderIndex é obrigatório")
        @Min(value = 0, message = "orderIndex deve ser >= 0")
        Integer orderIndex,

        @NotBlank(message = "inputPath é obrigatório")
        @Size(max = 500, message = "inputPath deve ter no máximo 500 caracteres")
        String inputPath,

        @NotBlank(message = "outputPath é obrigatório")
        @Size(max = 500, message = "outputPath deve ter no máximo 500 caracteres")
        String outputPath,

        // Pontuação deste caso de teste. null → padrão 0
        @Min(value = 0, message = "points deve ser >= 0")
        Integer points,

        // true = caso de exemplo visível ao usuário. null → padrão false
        Boolean isSample
) {}
