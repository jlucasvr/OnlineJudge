package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Tag;
import br.lab.testesubmissao.Repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Cria uma nova tag. Lança exceção se o nome já existir.
     */
    public Tag create(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new IllegalArgumentException("Tag '" + tag.getName() + "' já existe.");
        }
        return tagRepository.save(tag);
    }

    /**
     * Busca ou cria uma tag pelo nome.
     * Útil ao associar tags a problemas sem precisar verificar manualmente.
     */
    public Tag findOrCreate(String name) {
        return tagRepository.findByName(name).orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(name);
            return tagRepository.save(newTag);
        });
    }

    /**
     * Busca tag por ID.
     */
    public Tag findById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag não encontrada: " + id));
    }

    /**
     * Busca tag pelo nome.
     */
    public Tag findByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Tag não encontrada: " + name));
    }

    /**
     * Lista todas as tags.
     */
    public List<Tag> listAll() {
        return tagRepository.findAll();
    }

    /**
     * Remove tag pelo ID.
     */
    public void delete(UUID id) {
        Tag tag = findById(id);
        tagRepository.delete(tag);
    }
}
