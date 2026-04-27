package br.lab.testesubmissao.Dto.problem;

import br.lab.testesubmissao.Entity.Difficulty;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProblemResponse(
        UUID id,
        String title,
        String statement,
        Difficulty difficulty,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        Boolean isPublic,
        UUID authorId,
        String authorUsername,
        List<String> tags,
        LocalDateTime createdAt
) {
    public static ProblemResponse fromEntity(Problem problem, List<Tag> tags) {
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getStatement(),
                problem.getDifficulty(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                problem.getIsPublic(),
                problem.getAuthor() != null ? problem.getAuthor().getId() : null,
                problem.getAuthor() != null ? problem.getAuthor().getUsername() : null,
                tags.stream().map(Tag::getName).toList(),
                problem.getCreatedAt()
        );
    }
}
