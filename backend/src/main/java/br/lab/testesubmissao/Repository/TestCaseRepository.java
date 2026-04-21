package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    List<TestCase> findByProblemOrderByOrderIndex(Problem problem);

    List<TestCase> findByProblemAndIsSampleTrue(Problem problem);

    List<TestCase> findByProblemAndIsSampleFalse(Problem problem);

    // ✅ CORRIGIDO: @Transactional + @Modifying obrigatório para delete derivado
    @Transactional
    @Modifying
    void deleteByProblem(Problem problem);
}
