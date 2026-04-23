package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.user.AdminUpdateUserRequest;
import br.lab.testesubmissao.Dto.user.UpdateMeRequest;
import br.lab.testesubmissao.Dto.user.UserResponse;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Usuários.
 *
 * Endpoints:
 *   GET    /users/me              → perfil do usuário autenticado
 *   PUT    /users/me              → atualiza username/email do próprio usuário
 *   GET    /users                 → lista todos os usuários (ADMIN)
 *   GET    /users/{id}            → detalhe de um usuário (ADMIN)
 *   PUT    /users/{id}            → atualiza qualquer campo, incluindo role (ADMIN)
 *   DELETE /users/{id}            → remove um usuário (ADMIN)
 */
@RestController
@RequestMapping("/users")
@Tag(name = "users", description = "Controller para salvar e editar dados do usuário")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // GET /users/me  →  perfil do usuário autenticado
    // -------------------------------------------------------------------------
    @GetMapping("/me")
    @Operation(summary = "Retorna dados do usuário autenticado", description = "Método para mostrar os dados do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso para interface do usuário")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    // -------------------------------------------------------------------------
    // PUT /users/me  →  usuário atualiza seus próprios dados (username / email)
    // -------------------------------------------------------------------------
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @Valid @RequestBody UpdateMeRequest request,
            Authentication authentication
    ) {
        User current = userService.findByUsername(authentication.getName());

        // Monta objeto de updates — só os campos não-nulos serão aplicados no service
        User updates = new User();
        updates.setUsername(request.username());
        updates.setEmail(request.email());
        // role = null → service não altera a role

        User updated = userService.update(current.getId(), updates);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    // -------------------------------------------------------------------------
    // GET /users  →  lista todos os usuários (ADMIN)
    // -------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> listAll() {
        List<UserResponse> response = userService.listAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /users/{id}  →  detalhe de um usuário (ADMIN)
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponse.fromEntity(userService.findById(id)));
    }

    // -------------------------------------------------------------------------
    // PUT /users/{id}  →  ADMIN atualiza qualquer campo, inclusive role
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> adminUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        User updates = new User();
        updates.setUsername(request.username());
        updates.setEmail(request.email());
        updates.setRole(request.role()); // pode ser null → service não altera

        User updated = userService.update(id, updates);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    // -------------------------------------------------------------------------
    // DELETE /users/{id}  →  remove um usuário (ADMIN)
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
