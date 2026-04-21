package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    // ✅ NOVO: buscar tag por nome (evitar duplicatas ao criar)
    Optional<Tag> findByName(String name);

    // ✅ NOVO: verificar se tag já existe
    boolean existsByName(String name);
}