package br.lab.testesubmissao.Dto.submission;

import br.lab.testesubmissao.Entity.Submission;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID userId,
        UUID problemId,
        String language,
        String codePath,
        String status,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        LocalDateTime submittedAt
) {
    public static SubmissionResponse fromEntity(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getUser().getId(),
                submission.getProblem().getId(),
                submission.getLanguage(),
                submission.getCodePath(),
                submission.getStatus().name(),
                submission.getExecutionTimeMs(),
                submission.getMemoryUsedKb(),
                submission.getSubmittedAt()
        );
    }
}
