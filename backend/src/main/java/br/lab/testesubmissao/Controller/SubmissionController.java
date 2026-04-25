package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.submission.CreateSubmissionRequest;
import br.lab.testesubmissao.Dto.submission.SubmissionResponse;
import br.lab.testesubmissao.Dto.submission.UpdateSubmissionStatusRequest;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.SubmissionStatus;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Service.ProblemService;
import br.lab.testesubmissao.Service.AuditLogService;
import br.lab.testesubmissao.Service.SubmissionService;
import br.lab.testesubmissao.Service.SubmissionWorkflowService;
import br.lab.testesubmissao.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ✅ REESCRITO: Controller funcional, sem métodos que não existem mais na Service.
 *
 * Endpoints disponíveis:
 *   POST   /submissions          → cria uma submissão
 *   GET    /submissions/{id}     → busca submissão por ID
 *   GET    /submissions/user/{userId}          → histórico de um usuário
 *   GET    /submissions/problem/{problemId}    → submissões de um problema
 *   PATCH  /submissions/{id}/status            → atualiza status (para o Worker usar)
 *   DELETE /submissions/{id}    → remove submissão
 */
@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionWorkflowService submissionWorkflowService;
    private final UserService userService;
    private final ProblemService problemService;
    private final AuditLogService auditLogService;

    public SubmissionController(SubmissionService submissionService,
                                 SubmissionWorkflowService submissionWorkflowService,
                                 UserService userService,
                                 ProblemService problemService,
                                 AuditLogService auditLogService) {
        this.submissionService = submissionService;
        this.submissionWorkflowService = submissionWorkflowService;
        this.userService = userService;
        this.problemService = problemService;
        this.auditLogService = auditLogService;
    }

    /**
     * Cria uma nova submissão.
     * Corpo esperado:
     * {
     *   "problemId": "uuid-do-problema",
     *   "language": "c",
     *   "sourceCode": "#include <stdio.h> ..."
     * }
     */
    @PostMapping
    public ResponseEntity<SubmissionResponse> create(
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication
    ) {
        User user = userService.findByUsername(authentication.getName());
        Problem problem = problemService.findById(request.problemId());

        Submission saved = submissionWorkflowService.createAndDispatch(
                user,
                problem,
                request.language(),
                request.sourceCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SubmissionResponse.fromEntity(saved));
    }

    /**
     * Busca uma submissão pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(SubmissionResponse.fromEntity(submissionService.findById(id)));
    }

    /**
     * Lista submissões de um usuário específico.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionResponse>> findByUser(@PathVariable UUID userId, Authentication authentication) {
        User authenticatedUser = userService.findByUsername(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !authenticatedUser.getId().equals(userId)) {
            throw new AccessDeniedException("Você só pode ver seu próprio histórico de submissões.");
        }

        User user = userService.findById(userId);
        return ResponseEntity.ok(submissionService.findByUser(user).stream().map(SubmissionResponse::fromEntity).toList());
    }

    /**
     * Lista submissões de um problema específico.
     */
    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionResponse>> findByProblem(@PathVariable UUID problemId) {
        Problem problem = problemService.findById(problemId);
        return ResponseEntity.ok(submissionService.findByProblem(problem).stream().map(SubmissionResponse::fromEntity).toList());
    }

    /**
     * Lista submissões por status (uso operacional/admin).
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> findByStatus(@PathVariable SubmissionStatus status) {
        return ResponseEntity.ok(submissionService.findByStatus(status).stream().map(SubmissionResponse::fromEntity).toList());
    }

    /**
     * Atualiza o status de uma submissão.
     * Usado pelo Worker após processar o código.
     * Corpo esperado:
     * {
     *   "status": "ACCEPTED",
     *   "executionTimeMs": 120,
     *   "memoryUsedKb": 2048
     * }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SubmissionResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubmissionStatusRequest request,
            Authentication authentication
    ) {
        Submission updated = submissionService.updateStatus(
                id, request.status(), request.executionTimeMs(), request.memoryUsedKb());
        auditLogService.logAdminAction(authentication.getName(), "submission_status_updated", "submission", id.toString(),
                "status=" + request.status());
        return ResponseEntity.ok(SubmissionResponse.fromEntity(updated));
    }

    /**
     * Remove uma submissão.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        submissionService.delete(id);
        auditLogService.logAdminAction(authentication.getName(), "submission_deleted", "submission", id.toString(), "manual_delete");
        return ResponseEntity.noContent().build();
    }
}
