package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Difficulty;
import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Exception.ResourceNotFoundException;
import br.lab.testesubmissao.Repository.ProblemRepository;
import br.lab.testesubmissao.Repository.TestCaseRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProblemService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;
    private final VerdictService verdictService;

    public ProblemService(ProblemRepository problemRepository,
                          TestCaseRepository testCaseRepository,
                          VerdictService verdictService) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.verdictService = verdictService;
    }


    public Problem create(Problem problem) {
        return problemRepository.save(problem);
    }

    public Problem findById(UUID id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problema não encontrado: " + id));
    }


    public Problem update(UUID id, Problem updates) {
        Problem existing = findById(id);

        existing.setTitle(updates.getTitle());
        existing.setStatement(updates.getStatement());
        existing.setDifficulty(updates.getDifficulty());
        existing.setMemoryLimitKb(updates.getMemoryLimitKb());
        existing.setTimeLimitMs(updates.getTimeLimitMs());
        existing.setIsPublic(updates.getIsPublic());

    
        if (updates.getAuthor() != null) {
            existing.setAuthor(updates.getAuthor());
        }

        return problemRepository.save(existing);
    }


    @Transactional
    public void delete(UUID id) {
        Problem existing = findById(id);
        verdictService.deleteByProblem(existing);
        testCaseRepository.deleteByProblem(existing);
        problemRepository.delete(existing);
    }

    /**
     * Lista todos os problemas (uso administrativo).
     */
    public List<Problem> listAll() {
        return problemRepository.findAll();
    }

    /**
     * Lista apenas problemas públicos (para usuários não autenticados ou listagem geral).
     */
    public List<Problem> listPublic() {
        return problemRepository.findByIsPublicTrue();
    }

    /**
     * Filtra problemas por dificuldade.
     */
    public List<Problem> findByDifficulty(Difficulty difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    /**
     * Filtra problemas públicos por dificuldade.
     */
    public List<Problem> findPublicByDifficulty(Difficulty difficulty) {
        return problemRepository.findByIsPublicTrueAndDifficulty(difficulty);
    }

    /**
     * Busca problemas criados por um autor.
     */
    public List<Problem> findByAuthor(User author) {
        return problemRepository.findByAuthor(author);
    }

    /**
     * Busca problemas por título (parcial, case-insensitive).
     */
    public List<Problem> searchByTitle(String title) {
        return problemRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Busca problemas públicos que contenham uma tag específica.
     */
    public List<Problem> findPublicByTag(String tagName) {
        return problemRepository.findPublicByTagName(tagName);
    }
}
