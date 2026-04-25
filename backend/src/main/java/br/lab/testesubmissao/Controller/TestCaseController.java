package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.testcase.CreateTestCaseRequest;
import br.lab.testesubmissao.Dto.testcase.TestCaseResponse;
import br.lab.testesubmissao.Dto.testcase.UpdateTestCaseRequest;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Service.AuditLogService;
import br.lab.testesubmissao.Service.ProblemService;
import br.lab.testesubmissao.Service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Casos de Teste.
 *
 * Endpoints:
 *   GET    /problems/{problemId}/test-cases/samples   → casos de exemplo (público)
 *   GET    /problems/{problemId}/test-cases            → todos os casos (ADMIN)
 *   GET    /problems/{problemId}/test-cases/{id}       → detalhe de um caso (ADMIN)
 *   POST   /problems/{problemId}/test-cases            → cria um caso de teste (ADMIN)
 *   PUT    /problems/{problemId}/test-cases/{id}       → atualiza um caso (ADMIN)
 *   DELETE /problems/{problemId}/test-cases/{id}       → remove um caso (ADMIN)
 *   DELETE /problems/{problemId}/test-cases            → remove todos os casos (ADMIN)
 *
 * Nota: os paths de input/output são caminhos internos no filesystem do servidor,
 * gerenciados pelo Worker. O admin informa esses paths ao cadastrar os test cases.
 */
@RestController
@RequestMapping("/problems/{problemId}/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final ProblemService problemService;
    private final AuditLogService auditLogService;

    public TestCaseController(TestCaseService testCaseService,
                              ProblemService problemService,
                              AuditLogService auditLogService) {
        this.testCaseService = testCaseService;
        this.problemService = problemService;
        this.auditLogService = auditLogService;
    }

    // -------------------------------------------------------------------------
    // GET /problems/{problemId}/test-cases/samples  →  casos visíveis ao usuário
    // -------------------------------------------------------------------------
    @GetMapping("/samples")
    public ResponseEntity<List<TestCaseResponse>> findSamples(@PathVariable UUID problemId) {
        Problem problem = problemService.findById(problemId);
        List<TestCaseResponse> response = testCaseService.findSamplesByProblem(problem).stream()
                .map(TestCaseResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/{problemId}/test-cases  →  todos os casos (ADMIN)
    // -------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TestCaseResponse>> findAll(@PathVariable UUID problemId) {
        Problem problem = problemService.findById(problemId);
        List<TestCaseResponse> response = testCaseService.findByProblem(problem).stream()
                .map(TestCaseResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/{problemId}/test-cases/{id}  →  detalhe de um caso (ADMIN)
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseResponse> findById(
            @PathVariable UUID problemId,
            @PathVariable UUID id
    ) {
        // Valida que o problema existe (lança 404 se não)
        problemService.findById(problemId);
        return ResponseEntity.ok(TestCaseResponse.fromEntity(testCaseService.findById(id)));
    }

    // -------------------------------------------------------------------------
    // POST /problems/{problemId}/test-cases  →  cria um caso de teste (ADMIN)
    // -------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseResponse> create(
            @PathVariable UUID problemId,
            @Valid @RequestBody CreateTestCaseRequest request,
            Authentication authentication
    ) {
        Problem problem = problemService.findById(problemId);

        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        testCase.setOrderIndex(request.orderIndex());
        testCase.setInputPath(request.inputPath());
        testCase.setOutputPath(request.outputPath());
        testCase.setPoints(request.points() != null ? request.points() : 0);
        testCase.setIsSample(request.isSample() != null ? request.isSample() : false);

        TestCase saved = testCaseService.create(testCase);
        auditLogService.logAdminAction(authentication.getName(), "test_case_created", "test_case", saved.getId().toString(),
                "problemId=" + problemId + " orderIndex=" + saved.getOrderIndex());
        return ResponseEntity.status(HttpStatus.CREATED).body(TestCaseResponse.fromEntity(saved));
    }

    // -------------------------------------------------------------------------
    // PUT /problems/{problemId}/test-cases/{id}  →  atualiza um caso (ADMIN)
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseResponse> update(
            @PathVariable UUID problemId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTestCaseRequest request,
            Authentication authentication
    ) {
        // Valida que o problema existe
        problemService.findById(problemId);

        TestCase updates = new TestCase();
        updates.setOrderIndex(request.orderIndex());
        updates.setInputPath(request.inputPath());
        updates.setOutputPath(request.outputPath());
        updates.setPoints(request.points());
        updates.setIsSample(request.isSample());

        TestCase updated = testCaseService.update(id, updates);
        auditLogService.logAdminAction(authentication.getName(), "test_case_updated", "test_case", updated.getId().toString(),
                "problemId=" + problemId + " orderIndex=" + updated.getOrderIndex());
        return ResponseEntity.ok(TestCaseResponse.fromEntity(updated));
    }

    // -------------------------------------------------------------------------
    // DELETE /problems/{problemId}/test-cases/{id}  →  remove um caso (ADMIN)
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID problemId,
            @PathVariable UUID id,
            Authentication authentication
    ) {
        problemService.findById(problemId);
        testCaseService.delete(id);
        auditLogService.logAdminAction(authentication.getName(), "test_case_deleted", "test_case", id.toString(),
                "problemId=" + problemId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // DELETE /problems/{problemId}/test-cases  →  remove todos os casos (ADMIN)
    // -------------------------------------------------------------------------
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAll(@PathVariable UUID problemId, Authentication authentication) {
        Problem problem = problemService.findById(problemId);
        testCaseService.deleteByProblem(problem);
        auditLogService.logAdminAction(authentication.getName(), "test_case_deleted_all", "problem", problemId.toString(),
                "bulk_delete");
        return ResponseEntity.noContent().build();
    }
}
