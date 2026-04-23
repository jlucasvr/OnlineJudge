package br.lab.testesubmissao.Dto.user;

import br.lab.testesubmissao.Entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Usado pelo ADMIN para atualizar qualquer campo de um usuário, incluindo role.
 * Todos os campos são opcionais — apenas os não-nulos serão splicados.
 */
public record AdminUpdateUserRequest(

        @Size(min = 3, max = 50, message = "username deve ter entre 3 e 50 caracteres")
        String username,

        @Email(message = "email inválido")
        @Size(max = 255, message = "email deve ter no máximo 255 caracteres")
        String email,

        Role role
) {}
