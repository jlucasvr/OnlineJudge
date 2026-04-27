package br.lab.testesubmissao.Dto.testcase;

import br.lab.testesubmissao.Entity.TestCase;

import java.util.UUID;

public record TestCaseResponse(
        UUID id,
        UUID problemId,
        Integer orderIndex,
        String inputPath,
        String outputPath,
        Integer points,
        Boolean isSample,
        Integer timeLimitMs,
        Integer memoryLimitKb
) {
    public static TestCaseResponse fromEntity(TestCase tc) {
        return new TestCaseResponse(
                tc.getId(),
                tc.getProblem().getId(),
                tc.getOrderIndex(),
                tc.getInputPath(),
                tc.getOutputPath(),
                tc.getPoints(),
                tc.getIsSample(),
                tc.getProblem().getTimeLimitMs(),
                tc.getProblem().getMemoryLimitKb()
        );
    }
}
