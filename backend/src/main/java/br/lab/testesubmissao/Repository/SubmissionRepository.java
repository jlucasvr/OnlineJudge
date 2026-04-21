// ============================================================
// FILE: Repository/SubmissionRepository.java
// ============================================================
package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    // ✅ NOVO: histórico de submissões de um usuário
    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    // ✅ NOVO: submissões de um usuário em um problema específico
    List<Submission> findByUserAndProblemOrderBySubmittedAtDesc(User user, Problem problem);

    // ✅ NOVO: submissões de um problema (para admin/estatísticas)
    List<Submission> findByProblemOrderBySubmittedAtDesc(Problem problem);

    // ✅ NOVO: submissões pendentes (para o worker da fila)
    List<Submission> findByStatus(String status);

    // ✅ NOVO: contar submissões aceitas de um usuário em um problema
    long countByUserAndProblemAndStatus(User user, Problem problem, String status);
}
