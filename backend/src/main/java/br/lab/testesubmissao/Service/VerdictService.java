package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Entity.Verdict;
import br.lab.testesubmissao.Exception.ResourceNotFoundException;
import br.lab.testesubmissao.Repository.VerdictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new ResourceNotFoundException("Veredicto não encontrado: " + id));
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

    /**
     * Remove todos os veredictos associados a uma submissão.
     * Necessário antes de excluir submissões para evitar violação de FK.
     */
    @Transactional
    public void deleteBySubmission(Submission submission) {
        verdictRepository.deleteBySubmission(submission);
    }

    /**
     * Remove todos os veredictos associados a um test case.
     * Necessário antes de excluir test cases para evitar violação de FK.
     */
    @Transactional
    public void deleteByTestCase(TestCase testCase) {
        verdictRepository.deleteByTestCase(testCase);
    }

    /**
     * Remove todos os veredictos associados aos test cases de um problema.
     * Necessário antes de excluir test cases para evitar violação de FK.
     */
    @Transactional
    public void deleteByProblem(Problem problem) {
        verdictRepository.deleteByTestCaseProblem(problem);
    }
}
