package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Service.ProblemService;
import br.lab.testesubmissao.Service.SubmissionService;
import br.lab.testesubmissao.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;
    private final ProblemService problemService;

    // ✅ Injeção via construtor (sem @Autowired — é a prática recomendada pelo Spring)
    public SubmissionController(SubmissionService submissionService,
                                 UserService userService,
                                 ProblemService problemService) {
        this.submissionService = submissionService;
        this.userService = userService;
        this.problemService = problemService;
    }

    /**
     * Cria uma nova submissão.
     * Corpo esperado:
     * {
     *   "userId": "uuid-do-usuario",
     *   "problemId": "uuid-do-problema",
     *   "language": "c",
     *   "codePath": "/submissions/codigo.c"
     * }
     *
     * Nota: codePath temporariamente recebido direto. Futuramente será um upload de arquivo.
     */
    @PostMapping
    public ResponseEntity<Submission> create(@RequestBody SubmissionRequest request) {
        User user = userService.findById(request.userId());
        Problem problem = problemService.findById(request.problemId());

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(request.language());
        submission.setCodePath(request.codePath());

        Submission saved = submissionService.create(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Busca uma submissão pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Submission> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(submissionService.findById(id));
    }

    /**
     * Lista submissões de um usuário específico.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Submission>> findByUser(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(submissionService.findByUser(user));
    }

    /**
     * Lista submissões de um problema específico.
     */
    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<Submission>> findByProblem(@PathVariable UUID problemId) {
        Problem problem = problemService.findById(problemId);
        return ResponseEntity.ok(submissionService.findByProblem(problem));
    }

    /**
     * Atualiza o status de uma submissão.
     * Usado pelo Worker após processar o código.
     * Corpo esperado:
     * {
     *   "status": "accepted",
     *   "executionTimeMs": 120,
     *   "memoryUsedKb": 2048
     * }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Submission> updateStatus(@PathVariable UUID id,
                                                    @RequestBody StatusUpdateRequest request) {
        Submission updated = submissionService.updateStatus(
                id, request.status(), request.executionTimeMs(), request.memoryUsedKb());
        return ResponseEntity.ok(updated);
    }

    /**
     * Remove uma submissão.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        submissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Records auxiliares (DTOs inline simples) ──────────────────────────────

    /**
     * DTO para criação de submissão.
     * Quando adicionar Spring Security, userId virá do token JWT, não do body.
     */
    record SubmissionRequest(
            UUID userId,
            UUID problemId,
            String language,
            String codePath
    ) {}

    /**
     * DTO para atualização de status pelo Worker.
     */
    record StatusUpdateRequest(
            String status,
            Integer executionTimeMs,
            Integer memoryUsedKb
    ) {}
}
