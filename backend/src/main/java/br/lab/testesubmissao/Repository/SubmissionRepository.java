// ============================================================
// FILE: Repository/SubmissionRepository.java
// ============================================================
package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.SubmissionStatus;
import br.lab.testesubmissao.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    List<Submission> findByUserAndProblemOrderBySubmittedAtDesc(User user, Problem problem);

    List<Submission> findByProblemOrderBySubmittedAtDesc(Problem problem);

    List<Submission> findByStatus(SubmissionStatus status);
  
    long countByUserAndProblemAndStatus(User user, Problem problem, SubmissionStatus status);
}
