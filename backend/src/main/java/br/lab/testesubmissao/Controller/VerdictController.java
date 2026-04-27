package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.verdict.VerdictResponse;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Service.SubmissionService;
import br.lab.testesubmissao.Service.VerdictService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Veredictos.
 *
 * Endpoints:
 *   GET /submissions/{submissionId}/verdicts   → lista veredictos de uma submissão
 *   GET /submissions/{submissionId}/verdicts/{id}  → detalhe de um veredicto
 *
 * Regras de acesso:
 *   - O dono da submissão pode ver seus próprios veredictos.
 *   - ADMIN pode ver qualquer veredicto.
 */
@RestController
@RequestMapping("/submissions/{submissionId}/verdicts")
public class VerdictController {

    private final VerdictService verdictService;
    private final SubmissionService submissionService;

    public VerdictController(VerdictService verdictService, SubmissionService submissionService) {
        this.verdictService = verdictService;
        this.submissionService = submissionService;
    }


    // GET /submissions/{submissionId}/verdicts
    // Lista todos os veredictos (por test case) de uma submissão.

    @GetMapping
    public ResponseEntity<List<VerdictResponse>> findBySubmission(
            @PathVariable UUID submissionId,
            Authentication authentication
    ) {
        Submission submission = submissionService.findById(submissionId);
        checkOwnerOrAdmin(submission, authentication);

        List<VerdictResponse> response = verdictService.findBySubmission(submission).stream()
                .map(VerdictResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    // Detalhe de um veredicto específico que o usuário pedir

    @GetMapping("/{id}")
    public ResponseEntity<VerdictResponse> findById(
            @PathVariable UUID submissionId,
            @PathVariable UUID id,
            Authentication authentication
    ) {
        Submission submission = submissionService.findById(submissionId);
        checkOwnerOrAdmin(submission, authentication);

        return ResponseEntity.ok(VerdictResponse.fromEntity(verdictService.findById(id)));
    }


    private void checkOwnerOrAdmin(Submission submission, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        boolean isOwner = submission.getUser().getUsername().equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Você não tem permissão para ver os veredictos desta submissão.");
        }
    }
}
