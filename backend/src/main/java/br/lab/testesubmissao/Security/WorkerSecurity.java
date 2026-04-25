package br.lab.testesubmissao.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("workerSecurity")
public class WorkerSecurity {

    private final String workerUsername;

    public WorkerSecurity(@Value("${oj.bootstrap.worker.username:}") String workerUsername) {
        this.workerUsername = workerUsername;
    }

    public boolean isWorker(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!StringUtils.hasText(workerUsername)) {
            return false;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return isAdmin && workerUsername.equals(authentication.getName());
    }
}
