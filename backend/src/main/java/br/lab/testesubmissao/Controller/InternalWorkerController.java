package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.submission.SubmissionResponse;
import br.lab.testesubmissao.Dto.submission.UpdateSubmissionStatusRequest;
import br.lab.testesubmissao.Dto.testcase.TestCaseResponse;
import br.lab.testesubmissao.Dto.verdict.CreateVerdictRequest;
import br.lab.testesubmissao.Dto.verdict.VerdictResponse;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Entity.Verdict;
import br.lab.testesubmissao.Service.ProblemService;
import br.lab.testesubmissao.Service.SubmissionService;
import br.lab.testesubmissao.Service.TestCaseService;
import br.lab.testesubmissao.Service.VerdictService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints internos usados exclusivamente pelo Worker.
 *
 * Protegidos por ROLE_ADMIN — o Worker autentica com credenciais de uma conta
 * de serviço administrativa (worker_service_account.sql).
 *
 * Endpoints:
 *   GET  /internal/problems/{problemId}/test-cases  → lista test cases para o Worker
 *   POST /internal/verdicts                         → salva veredicto de um test case
 */
@RestController
@RequestMapping("/internal")
@PreAuthorize("@workerSecurity.isWorker(authentication)")
public class InternalWorkerController {

    private static final int ACTUAL_OUTPUT_MAX_CHARS = 4096;

    private final VerdictService verdictService;
    private final SubmissionService submissionService;
    private final TestCaseService testCaseService;
    private final ProblemService problemService;

    public InternalWorkerController(VerdictService verdictService,
                                    SubmissionService submissionService,
                                    TestCaseService testCaseService,
                                    ProblemService problemService) {
        this.verdictService = verdictService;
        this.submissionService = submissionService;
        this.testCaseService = testCaseService;
        this.problemService = problemService;
    }

    /**
     * Lista todos os test cases de um problema, ordenados por order_index.
     * Usado pelo Worker para saber quais casos executar e onde estão os arquivos.
     */
    @GetMapping("/problems/{problemId}/test-cases")
    public ResponseEntity<List<TestCaseResponse>> listTestCases(@PathVariable UUID problemId) {
        Problem problem = problemService.findById(problemId);
        List<TestCaseResponse> response = testCaseService.findByProblem(problem)
                .stream()
                .map(TestCaseResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza o status de uma submissão.
     * Endpoint interno usado pelo Worker para evitar depender da rota administrativa pública.
     */
    @PatchMapping("/submissions/{submissionId}/status")
    public ResponseEntity<SubmissionResponse> updateSubmissionStatus(
            @PathVariable UUID submissionId,
            @Valid @RequestBody UpdateSubmissionStatusRequest request
    ) {
        Submission updated = submissionService.updateStatus(
                submissionId,
                request.status(),
                request.executionTimeMs(),
                request.memoryUsedKb()
        );
        return ResponseEntity.ok(SubmissionResponse.fromEntity(updated));
    }

    /**
     * Salva o veredicto de um test case.
     *
     * Regra de negócio:
     *   - actual_output só é persistido se o test_case tiver is_sample = true.
     *   - Se o output ultrapassar ACTUAL_OUTPUT_MAX_CHARS, é truncado.
     */
    @PostMapping("/verdicts")
    public ResponseEntity<VerdictResponse> createVerdict(
            @Valid @RequestBody CreateVerdictRequest request
    ) {
        Submission submission = submissionService.findById(request.submissionId());
        TestCase testCase = testCaseService.findById(request.testCaseId());

        Verdict verdict = new Verdict();
        verdict.setSubmission(submission);
        verdict.setTestCase(testCase);
        verdict.setResult(request.result());
        verdict.setExecutionTimeMs(request.executionTimeMs());
        verdict.setMemoryUsedKb(request.memoryUsedKb());
        verdict.setCheckerOutput(request.checkerOutput());

        // actual_output só salvo para cases de exemplo (is_sample = true)
        if (Boolean.TRUE.equals(testCase.getIsSample()) && request.actualOutput() != null) {
            verdict.setActualOutput(truncate(request.actualOutput(), ACTUAL_OUTPUT_MAX_CHARS));
        }

        Verdict saved = verdictService.save(verdict);
        return ResponseEntity.status(HttpStatus.CREATED).body(VerdictResponse.fromEntity(saved));
    }

    private String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n... [saída truncada]";
    }
}
