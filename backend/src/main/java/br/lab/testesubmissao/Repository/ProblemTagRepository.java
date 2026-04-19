package br.lab.testesubmissao.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import br.lab.testesubmissao.Entity.ProblemTag;
import br.lab.testesubmissao.Entity.ProblemTagId;

public interface ProblemTagRepository extends JpaRepository<ProblemTag, ProblemTagId> {
    
}
