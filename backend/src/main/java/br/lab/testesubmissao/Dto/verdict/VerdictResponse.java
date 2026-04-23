package br.lab.testesubmissao.Dto.verdict;

import br.lab.testesubmissao.Entity.Verdict;

import java.util.UUID;

public record VerdictResponse(
        UUID id,
        UUID submissionId,
        UUID testCaseId,
        Integer testCaseOrderIndex,
        Boolean testCaseIsSample,
        String result,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        String checkerOutput
) {
    public static VerdictResponse fromEntity(Verdict verdict) {
        return new VerdictResponse(
                verdict.getId(),
                verdict.getSubmission().getId(),
                verdict.getTestCase().getId(),
                verdict.getTestCase().getOrderIndex(),
                verdict.getTestCase().getIsSample(),
                verdict.getResult(),
                verdict.getExecutionTimeMs(),
                verdict.getMemoryUsedKb(),
                verdict.getCheckerOutput()
        );
    }
}
