package br.lab.testesubmissao.Dto.problem;

import jakarta.validation.constraints.*;

import java.util.List;

import br.lab.testesubmissao.Entity.Difficulty;


public record CreateProblemRequest(

        @NotBlank(message = "title é obrigatório")
        @Size(max = 255, message = "title deve ter no máximo 255 caracteres")
        String title,

        @NotBlank(message = "statement é obrigatório")
        String statement,

        @NotNull(message = "difficulty é obrigatório")
        Difficulty difficulty,

        @NotNull(message = "timeLimitMs é obrigatório")
        @Min(value = 100, message = "timeLimitMs deve ser pelo menos 100ms")
        @Max(value = 30000, message = "timeLimitMs não pode exceder 30000ms")
        Integer timeLimitMs,

        @NotNull(message = "memoryLimitKb é obrigatório")
        @Min(value = 1024, message = "memoryLimitKb deve ser pelo menos 1024 KB (1 MB)")
        @Max(value = 524288, message = "memoryLimitKb não pode exceder 524288 KB (512 MB)")
        Integer memoryLimitKb,

        // Se não informado, o problema será criado como privado
        Boolean isPublic,

        // Lista de nomes de tags a associar (opcional)
        List<String> tags
) {}
