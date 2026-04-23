package br.lab.testesubmissao.Dto.auth;

import java.util.UUID;

import br.lab.testesubmissao.Entity.Role;

public record AuthResponse(
        UUID userId,
        String username,
        Role role,
        String token
) {
}
