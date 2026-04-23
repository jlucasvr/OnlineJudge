package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.problem.CreateProblemRequest;
import br.lab.testesubmissao.Dto.problem.ProblemResponse;
import br.lab.testesubmissao.Dto.problem.UpdateProblemRequest;
import br.lab.testesubmissao.Entity.Difficulty;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Service.ProblemService;
import br.lab.testesubmissao.Service.ProblemTagService;
import br.lab.testesubmissao.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Problemas.
 *
 * Endpoints:
 *   GET    /problems                     → lista problemas públicos (com filtros opcionais)
 *   GET    /problems/all                 → lista todos os problemas (ADMIN)
 *   GET    /problems/{id}                → detalhe de um problema
 *   GET    /problems/search?title=       → busca por título (case-insensitive)
 *   GET    /problems/tag/{tagName}       → filtra públicos por tag
 *   POST   /problems                     → cria problema (ADMIN)
 *   PUT    /problems/{id}                → atualiza problema (ADMIN)
 *   PATCH  /problems/{id}/tags           → redefine as tags do problema (ADMIN)
 *   DELETE /problems/{id}                → remove problema (ADMIN)
 */
@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;
    private final ProblemTagService problemTagService;
    private final UserService userService;

    public ProblemController(ProblemService problemService,
                             ProblemTagService problemTagService,
                             UserService userService) {
        this.problemService = problemService;
        this.problemTagService = problemTagService;
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // GET /problems  →  lista problemas públicos, com filtros opcionais
    // GET /problems?difficulty=EASY
    // -------------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<ProblemResponse>> listPublic(
            @RequestParam(required = false) Difficulty difficulty
    ) {
    List<Problem> problems = (difficulty != null)
            ? problemService.findPublicByDifficulty(difficulty)
            : problemService.listPublic();

        List<ProblemResponse> response = problems.stream()
                .map(p -> ProblemResponse.fromEntity(p, problemTagService.getTagsOfProblem(p)))
                .toList();

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/all  →  todos os problemas, inclusive privados (ADMIN)
    // -------------------------------------------------------------------------
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProblemResponse>> listAll() {
        List<ProblemResponse> response = problemService.listAll().stream()
                .map(p -> ProblemResponse.fromEntity(p, problemTagService.getTagsOfProblem(p)))
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/search?title=soma  →  busca por título parcial
    // -------------------------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<List<ProblemResponse>> search(@RequestParam String title) {
        List<ProblemResponse> response = problemService.searchByTitle(title).stream()
                .map(p -> ProblemResponse.fromEntity(p, problemTagService.getTagsOfProblem(p)))
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/tag/{tagName}  →  públicos com uma tag específica
    // -------------------------------------------------------------------------
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<ProblemResponse>> findByTag(@PathVariable String tagName) {
        List<ProblemResponse> response = problemService.findPublicByTag(tagName).stream()
                .map(p -> ProblemResponse.fromEntity(p, problemTagService.getTagsOfProblem(p)))
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /problems/{id}  →  detalhe de um problema pelo ID
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> findById(@PathVariable UUID id) {
        Problem problem = problemService.findById(id);
        return ResponseEntity.ok(
                ProblemResponse.fromEntity(problem, problemTagService.getTagsOfProblem(problem))
        );
    }

    // -------------------------------------------------------------------------
    // POST /problems  →  cria um novo problema (ADMIN)
    // -------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> create(
            @Valid @RequestBody CreateProblemRequest request,
            Authentication authentication
    ) {
        User author = userService.findByUsername(authentication.getName());

        Problem problem = new Problem();
        problem.setTitle(request.title());
        problem.setStatement(request.statement());
        problem.setDifficulty(request.difficulty());
        problem.setTimeLimitMs(request.timeLimitMs());
        problem.setMemoryLimitKb(request.memoryLimitKb());
        problem.setIsPublic(request.isPublic() != null ? request.isPublic() : false);
        problem.setAuthor(author);

        Problem saved = problemService.create(problem);

        if (request.tags() != null && !request.tags().isEmpty()) {
            problemTagService.setTags(saved, request.tags());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ProblemResponse.fromEntity(saved, problemTagService.getTagsOfProblem(saved))
        );
    }

    // -------------------------------------------------------------------------
    // PUT /problems/{id}  →  atualiza um problema existente (ADMIN)
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProblemRequest request
    ) {
        Problem updates = new Problem();
        updates.setTitle(request.title());
        updates.setStatement(request.statement());
        updates.setDifficulty(request.difficulty());
        updates.setTimeLimitMs(request.timeLimitMs());
        updates.setMemoryLimitKb(request.memoryLimitKb());
        updates.setIsPublic(request.isPublic());

        Problem updated = problemService.update(id, updates);

        if (request.tags() != null) {
            problemTagService.setTags(updated, request.tags());
        }

        return ResponseEntity.ok(
                ProblemResponse.fromEntity(updated, problemTagService.getTagsOfProblem(updated))
        );
    }

    // -------------------------------------------------------------------------
    // PATCH /problems/{id}/tags  →  redefine apenas as tags (ADMIN)
    // -------------------------------------------------------------------------
    @PatchMapping("/{id}/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> updateTags(
            @PathVariable UUID id,
            @RequestBody List<String> tagNames
    ) {
        Problem problem = problemService.findById(id);
        problemTagService.setTags(problem, tagNames);
        return ResponseEntity.ok(
                ProblemResponse.fromEntity(problem, problemTagService.getTagsOfProblem(problem))
        );
    }

    // -------------------------------------------------------------------------
    // DELETE /problems/{id}  →  remove um problema (ADMIN)
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        // Limpa tags e test cases antes de deletar (evita violação de FK)
        Problem problem = problemService.findById(id);
        problemTagService.removeAllTagsFromProblem(problem);
        problemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
