package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    /**
     * Cria um novo problema.
     */
    public Problem create(Problem problem) {
        return problemRepository.save(problem);
    }

    /**
     * Busca problema por ID. Lança exceção se não encontrado.
     * ✅ CORRIGIDO: mensagem de erro era "Submissão não encontrada" (cópia/cola errada).
     */
    public Problem findById(UUID id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problema não encontrado: " + id));
    }

    /**
     * Atualiza um problema existente.
     * ✅ CORRIGIDO: update agora retorna o objeto salvo.
     * ✅ CORRIGIDO: createdAt não deve ser atualizado (data de criação é imutável).
     */
    public Problem update(UUID id, Problem updates) {
        Problem existing = findById(id);

        existing.setTitle(updates.getTitle());
        existing.setStatement(updates.getStatement());
        existing.setDifficulty(updates.getDifficulty());
        existing.setMemoryLimitKb(updates.getMemoryLimitKb());
        existing.setTimeLimitMs(updates.getTimeLimitMs());
        existing.setIsPublic(updates.getIsPublic());

        // ✅ Autor pode ser transferido (ex: admin reatribuindo)
        if (updates.getAuthor() != null) {
            existing.setAuthor(updates.getAuthor());
        }

        return problemRepository.save(existing);
    }

    /**
     * Remove um problema pelo ID.
     */
    public void delete(UUID id) {
        Problem existing = findById(id);
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
    public List<Problem> findByDifficulty(String difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    /**
     * Filtra problemas públicos por dificuldade.
     */
    public List<Problem> findPublicByDifficulty(String difficulty) {
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
