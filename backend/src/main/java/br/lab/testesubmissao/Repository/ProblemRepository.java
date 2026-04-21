// ============================================================
// FILE: Repository/ProblemRepository.java
// ============================================================
package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    // Já existia
    List<Problem> findByDifficulty(String difficulty);

    // ✅ NOVO: buscar apenas problemas públicos (para listagem sem auth)
    List<Problem> findByIsPublicTrue();

    // ✅ NOVO: buscar problemas públicos por dificuldade
    List<Problem> findByIsPublicTrueAndDifficulty(String difficulty);

    // ✅ NOVO: buscar problemas de um autor específico
    List<Problem> findByAuthor(User author);

    // ✅ NOVO: busca por título (case-insensitive, parcial)
    List<Problem> findByTitleContainingIgnoreCase(String title);

    // ✅ NOVO: problemas com uma tag específica (via JOIN)
    @Query("SELECT pt.problem FROM ProblemTag pt WHERE pt.tag.name = :tagName AND pt.problem.isPublic = true")
    List<Problem> findPublicByTagName(@Param("tagName") String tagName);
}
