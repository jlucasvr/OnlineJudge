package br.lab.testesubmissao.Dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Usado pelo próprio usuário para atualizar username e/ou email.
 * Todos os campos são opcionais — apenas os não-nulos serão aplicados.
 * Senha e role não são alterados por aqui.
 */
public record UpdateMeRequest(

        @Size(min = 3, max = 50, message = "username deve ter entre 3 e 50 caracteres")
        String username,

        @Email(message = "email inválido")
        @Size(max = 255, message = "email deve ter no máximo 255 caracteres")
        String email
) {}
