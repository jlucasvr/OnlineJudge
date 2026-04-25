package br.lab.testesubmissao.Dto.testcase;

import br.lab.testesubmissao.Entity.Difficulty;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.TestCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCaseResponseTest {

    @Test
    void fromEntityIncludesProblemExecutionLimits() {
        UUID problemId = UUID.randomUUID();
        UUID testCaseId = UUID.randomUUID();

        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setDifficulty(Difficulty.EASY);
        problem.setTimeLimitMs(1500);
        problem.setMemoryLimitKb(131072);

        TestCase testCase = new TestCase();
        testCase.setId(testCaseId);
        testCase.setProblem(problem);
        testCase.setOrderIndex(2);
        testCase.setInputPath("/data/testcases/p1/inputs/2");
        testCase.setOutputPath("/data/testcases/p1/outputs/2");
        testCase.setPoints(30);
        testCase.setIsSample(false);

        TestCaseResponse response = TestCaseResponse.fromEntity(testCase);

        assertEquals(testCaseId, response.id());
        assertEquals(problemId, response.problemId());
        assertEquals(1500, response.timeLimitMs());
        assertEquals(131072, response.memoryLimitKb());
    }
}
