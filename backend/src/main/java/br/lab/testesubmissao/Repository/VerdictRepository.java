package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Entity.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerdictRepository extends JpaRepository<Verdict, UUID> {

    List<Verdict> findBySubmission(Submission submission);

    List<Verdict> findBySubmissionAndTestCase(Submission submission, TestCase testCase);
}
