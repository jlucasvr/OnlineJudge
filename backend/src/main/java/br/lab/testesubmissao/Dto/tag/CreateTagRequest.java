package br.lab.testesubmissao.Dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(

        @NotBlank(message = "name é obrigatório")
        @Size(min = 2, max = 100, message = "name deve ter entre 2 e 100 caracteres")
        String name
) {}
