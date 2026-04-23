package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.tag.CreateTagRequest;
import br.lab.testesubmissao.Dto.tag.TagResponse;
import br.lab.testesubmissao.Entity.Tag;
import br.lab.testesubmissao.Service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Tags.
 *
 * Endpoints:
 *   GET    /tags          → lista todas as tags (público)
 *   GET    /tags/{id}     → detalhe de uma tag
 *   POST   /tags          → cria uma tag (ADMIN)
 *   DELETE /tags/{id}     → remove uma tag (ADMIN)
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // -------------------------------------------------------------------------
    // GET /tags  →  lista todas as tags disponíveis
    // -------------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<TagResponse>> listAll() {
        List<TagResponse> response = tagService.listAll().stream()
                .map(TagResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /tags/{id}  →  busca uma tag pelo ID
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(TagResponse.fromEntity(tagService.findById(id)));
    }

    // -------------------------------------------------------------------------
    // POST /tags  →  cria uma nova tag (ADMIN)
    // -------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.name());

        Tag saved = tagService.create(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(TagResponse.fromEntity(saved));
    }

    // -------------------------------------------------------------------------
    // DELETE /tags/{id}  →  remove uma tag (ADMIN)
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
