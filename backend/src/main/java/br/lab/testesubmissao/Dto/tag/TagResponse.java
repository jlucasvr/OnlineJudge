package br.lab.testesubmissao.Dto.tag;

import br.lab.testesubmissao.Entity.Tag;

import java.util.UUID;

public record TagResponse(
        UUID id,
        String name
) {
    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
