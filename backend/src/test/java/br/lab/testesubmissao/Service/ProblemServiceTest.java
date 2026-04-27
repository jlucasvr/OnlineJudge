package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Repository.ProblemRepository;
import br.lab.testesubmissao.Repository.TestCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private VerdictService verdictService;

    @InjectMocks
    private ProblemService problemService;

    @Test
    void deleteRemovesVerdictsBeforeTestCasesAndProblem() {
        UUID problemId = UUID.randomUUID();
        Problem problem = new Problem();
        problem.setId(problemId);

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        problemService.delete(problemId);

        InOrder inOrder = inOrder(problemRepository, verdictService, testCaseRepository);
        inOrder.verify(problemRepository).findById(problemId);
        inOrder.verify(verdictService).deleteByProblem(problem);
        inOrder.verify(testCaseRepository).deleteByProblem(problem);
        inOrder.verify(problemRepository).delete(problem);
        verifyNoMoreInteractions(problemRepository, verdictService, testCaseRepository);
    }
}
