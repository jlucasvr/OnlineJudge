package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}
