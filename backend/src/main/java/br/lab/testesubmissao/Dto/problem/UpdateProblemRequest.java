package br.lab.testesubmissao.Dto.problem;

import jakarta.validation.constraints.*;

import java.util.List;

import br.lab.testesubmissao.Entity.Difficulty;

public record UpdateProblemRequest(

        @NotBlank(message = "title é obrigatório")
        @Size(max = 255, message = "title deve ter no máximo 255 caracteres")
        String title,

        @NotBlank(message = "statement é obrigatório")
        String statement,

        @NotBlank(message = "difficulty é obrigatório")
        @Pattern(
                regexp = "^(EASY|MEDIUM|HARD)$",
                message = "difficulty deve ser EASY, MEDIUM ou HARD"
        )
        Difficulty difficulty,

        @NotNull(message = "timeLimitMs é obrigatório")
        @Min(value = 100, message = "timeLimitMs deve ser pelo menos 100ms")
        @Max(value = 30000, message = "timeLimitMs não pode exceder 30000ms")
        Integer timeLimitMs,

        @NotNull(message = "memoryLimitKb é obrigatório")
        @Min(value = 1024, message = "memoryLimitKb deve ser pelo menos 1024 KB")
        @Max(value = 524288, message = "memoryLimitKb não pode exceder 524288 KB")
        Integer memoryLimitKb,

        @NotNull(message = "isPublic é obrigatório")
        Boolean isPublic,

        // null = não altera as tags; lista vazia = remove todas as tags
        List<String> tags
) {}
