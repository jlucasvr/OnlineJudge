package br.lab.testesubmissao.Security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerSecurityTest {

    @Test
    void acceptsConfiguredWorkerWithAdminRole() {
        WorkerSecurity workerSecurity = new WorkerSecurity("worker");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "worker",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertTrue(workerSecurity.isWorker(authentication));
    }

    @Test
    void rejectsOtherAdmins() {
        WorkerSecurity workerSecurity = new WorkerSecurity("worker");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "alice",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertFalse(workerSecurity.isWorker(authentication));
    }

    @Test
    void rejectsMissingConfiguredWorkerUsername() {
        WorkerSecurity workerSecurity = new WorkerSecurity("");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "worker",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertFalse(workerSecurity.isWorker(authentication));
    }
}
