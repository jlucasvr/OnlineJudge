package br.lab.testesubmissao.Config;

import br.lab.testesubmissao.Entity.Role;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class WorkerAccountBootstrap {

    private static final Logger log = LoggerFactory.getLogger(WorkerAccountBootstrap.class);

    @Bean
    public ApplicationRunner bootstrapWorkerAccount(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${oj.bootstrap.worker.enabled:true}") boolean enabled,
            @Value("${oj.bootstrap.worker.username:worker}") String username,
            @Value("${oj.bootstrap.worker.email:worker@internal.oj}") String email,
            @Value("${oj.bootstrap.worker.password:}") String rawPassword
    ) {
        return args -> {
            if (!enabled) {
                log.info("Bootstrap da conta do worker desabilitado.");
                return;
            }

            if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
                log.warn("Bootstrap da conta do worker ignorado: configure username, email e password.");
                return;
            }

            User worker = userRepository.findByUsername(username).orElseGet(User::new);
            boolean isNew = worker.getId() == null;

            worker.setUsername(username);
            worker.setEmail(email);
            worker.setPasswordHash(passwordEncoder.encode(rawPassword));
            worker.setRole(Role.ROLE_ADMIN);

            userRepository.save(worker);
            log.info("Conta de serviço do worker {} com sucesso.", isNew ? "criada" : "atualizada");
        };
    }
}
