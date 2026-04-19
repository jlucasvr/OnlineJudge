package br.lab.testesubmissao.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.lab.testesubmissao.Entity.Verdict;

public interface VerdictReposity extends JpaRepository<Verdict, UUID>{
    
}
