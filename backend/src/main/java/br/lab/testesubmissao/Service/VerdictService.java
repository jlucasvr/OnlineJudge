package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Entity.Verdict;
import br.lab.testesubmissao.Repository.VerdictRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VerdictService {

    private final VerdictRepository verdictRepository;

    public VerdictService(VerdictRepository verdictRepository) {
        this.verdictRepository = verdictRepository;
    }

    /**
     * Salva o veredicto de um caso de teste.
     * Normalmente chamado pelo Worker após executar cada test case.
     */
    public Verdict save(Verdict verdict) {
        return verdictRepository.save(verdict);
    }

    /**
     * Busca veredicto por ID.
     */
    public Verdict findById(UUID id) {
        return verdictRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veredicto não encontrado: " + id));
    }

    /**
     * Lista todos os veredictos de uma submissão.
     */
    public List<Verdict> findBySubmission(Submission submission) {
        return verdictRepository.findBySubmission(submission);
    }

    /**
     * Busca o veredicto de uma submissão em um caso de teste específico.
     */
    public List<Verdict> findBySubmissionAndTestCase(Submission submission, TestCase testCase) {
        return verdictRepository.findBySubmissionAndTestCase(submission, testCase);
    }
}