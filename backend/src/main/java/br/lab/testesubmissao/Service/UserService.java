package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Role;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Exception.ResourceNotFoundException;
import br.lab.testesubmissao.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' já está em uso.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' já está cadastrado.");
        }

        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Busca usuário por ID. Lança exceção se não encontrado.
     */
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    /**
     * Busca usuário por username. Útil para login e exibição de perfil.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
    }

    /**
     * Lista todos os usuários (uso administrativo).
     */
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * Atualiza dados básicos do usuário (username, email, role).
     * Não atualiza senha aqui — senha tem fluxo próprio.
     */
    public User update(UUID id, User updates) {
        User existing = findById(id);

        if (updates.getUsername() != null && !updates.getUsername().equals(existing.getUsername())) {
            if (userRepository.existsByUsername(updates.getUsername())) {
                throw new IllegalArgumentException("Username '" + updates.getUsername() + "' já está em uso.");
            }
            existing.setUsername(updates.getUsername());
        }

        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(updates.getEmail())) {
                throw new IllegalArgumentException("Email '" + updates.getEmail() + "' já está cadastrado.");
            }
            existing.setEmail(updates.getEmail());
        }

        if (updates.getRole() != null) {
            existing.setRole(updates.getRole());
        }

        return userRepository.save(existing);
    }

    /**
     * Remove um usuário pelo ID.
     */
    public void delete(UUID id) {
        User existing = findById(id);
        userRepository.delete(existing);
    }
}
