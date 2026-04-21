package br.lab.testesubmissao.Repository;

import br.lab.testesubmissao.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // ✅ NOVO: buscar por username (login)
    Optional<User> findByUsername(String username);

    // ✅ NOVO: buscar por email (registro / recuperação)
    Optional<User> findByEmail(String email);

    // ✅ NOVO: verificar duplicatas no cadastro
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
