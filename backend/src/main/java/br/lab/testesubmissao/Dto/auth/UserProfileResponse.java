package br.lab.testesubmissao.Dto.auth;

import java.util.UUID;

import br.lab.testesubmissao.Entity.Role;

public record UserProfileResponse(
        UUID userId,
        String username,
        String email,
        Role role
) {
}
