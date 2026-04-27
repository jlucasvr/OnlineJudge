package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.ProblemTag;
import br.lab.testesubmissao.Entity.Tag;
import br.lab.testesubmissao.Repository.ProblemTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProblemTagService {

    private final ProblemTagRepository problemTagRepository;
    private final TagService tagService;

    public ProblemTagService(ProblemTagRepository problemTagRepository, TagService tagService) {
        this.problemTagRepository = problemTagRepository;
        this.tagService = tagService;
    }

    /**
     * Associa uma tag a um problema.
     */
    public ProblemTag addTag(Problem problem, Tag tag) {
        ProblemTag pt = new ProblemTag(problem, tag);
        return problemTagRepository.save(pt);
    }

    /**
     * Associa uma tag por nome a um problema.
     * Cria a tag se ela não existir.
     */
    public ProblemTag addTagByName(Problem problem, String tagName) {
        Tag tag = tagService.findOrCreate(tagName);
        return addTag(problem, tag);
    }

    /**
     * Remove a associação entre um problema e uma tag.
     */
    public void removeTag(Problem problem, Tag tag) {
        problemTagRepository.findByProblem(problem).stream()
                .filter(pt -> pt.getTag().getId().equals(tag.getId()))
                .findFirst()
                .ifPresent(problemTagRepository::delete);
    }

    /**
     * Lista todas as tags de um problema.
     */
    public List<Tag> getTagsOfProblem(Problem problem) {
        return problemTagRepository.findByProblem(problem).stream()
                .map(ProblemTag::getTag)
                .collect(Collectors.toList());
    }

    /**
     * Remove todas as tags de um problema.
     * Útil ao deletar o problema.
     */
    @Transactional
    public void removeAllTagsFromProblem(Problem problem) {
        problemTagRepository.deleteByProblem(problem);
    }

    /**
     * Redefine completamente as tags de um problema.
     * Remove as antigas e adiciona as novas.
     */
    @Transactional
    public void setTags(Problem problem, List<String> tagNames) {
        removeAllTagsFromProblem(problem);
        for (String name : tagNames) {
            addTagByName(problem, name);
        }
    }
}
