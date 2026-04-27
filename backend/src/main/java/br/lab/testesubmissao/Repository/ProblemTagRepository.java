package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.ProblemTag;
import br.lab.testesubmissao.Entity.ProblemTagId;
import br.lab.testesubmissao.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProblemTagRepository extends JpaRepository<ProblemTag, ProblemTagId> {

    List<ProblemTag> findByProblem(Problem problem);

    List<ProblemTag> findByTag(Tag tag);

    @Transactional
    @Modifying
    void deleteByProblem(Problem problem);
}
