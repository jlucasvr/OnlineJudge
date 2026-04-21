package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Entity.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerdictRepository extends JpaRepository<Verdict, UUID> {

    // ✅ NOVO: todos os veredictos de uma submissão
    List<Verdict> findBySubmission(Submission submission);

    // ✅ NOVO: veredicto de uma submissão em um caso de teste específico
    List<Verdict> findBySubmissionAndTestCase(Submission submission, TestCase testCase);
}
