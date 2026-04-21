package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.TestCase;
import br.lab.testesubmissao.Repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;

    public TestCaseService(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    /**
     * Adiciona um caso de teste a um problema.
     */
    public TestCase create(TestCase testCase) {
        return testCaseRepository.save(testCase);
    }

    /**
     * Busca caso de teste por ID.
     */
    public TestCase findById(UUID id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso de teste não encontrado: " + id));
    }

    /**
     * Lista todos os casos de teste de um problema, ordenados por order_index.
     */
    public List<TestCase> findByProblem(Problem problem) {
        return testCaseRepository.findByProblemOrderByOrderIndex(problem);
    }

    /**
     * Lista apenas os casos de teste de exemplo (visíveis ao usuário no enunciado).
     */
    public List<TestCase> findSamplesByProblem(Problem problem) {
        return testCaseRepository.findByProblemAndIsSampleTrue(problem);
    }

    /**
     * Lista apenas os casos de teste privados (usados pelo judge, não visíveis ao usuário).
     */
    public List<TestCase> findPrivateByProblem(Problem problem) {
        return testCaseRepository.findByProblemAndIsSampleFalse(problem);
    }

    /**
     * Atualiza um caso de teste.
     */
    public TestCase update(UUID id, TestCase updates) {
        TestCase existing = findById(id);
        existing.setInputPath(updates.getInputPath());
        existing.setOutputPath(updates.getOutputPath());
        existing.setOrderIndex(updates.getOrderIndex());
        existing.setPoints(updates.getPoints());
        existing.setIsSample(updates.getIsSample());
        return testCaseRepository.save(existing);
    }

    /**
     * Remove um caso de teste pelo ID.
     */
    public void delete(UUID id) {
        TestCase existing = findById(id);
        testCaseRepository.delete(existing);
    }

    /**
     * Remove todos os casos de teste de um problema.
     * Usado ao deletar o problema inteiro.
     */
    @Transactional
    public void deleteByProblem(Problem problem) {
        testCaseRepository.deleteByProblem(problem);
    }
}