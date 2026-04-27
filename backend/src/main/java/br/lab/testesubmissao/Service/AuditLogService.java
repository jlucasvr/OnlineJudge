package br.lab.testesubmissao.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void logSubmission(String actor, String action, String submissionId, String details) {
        auditLog.info("event=submission action={} actor={} submissionId={} details=\"{}\"",
                action, sanitize(actor), sanitize(submissionId), sanitize(details));
    }

    public void logAdminAction(String actor, String action, String resourceType, String resourceId, String details) {
        auditLog.info("event=admin_action action={} actor={} resourceType={} resourceId={} details=\"{}\"",
                action, sanitize(actor), sanitize(resourceType), sanitize(resourceId), sanitize(details));
    }

    public void logUserAction(String actor, String action, String resourceType, String resourceId, String details) {
        auditLog.info("event=user_action action={} actor={} resourceType={} resourceId={} details=\"{}\"",
                action, sanitize(actor), sanitize(resourceType), sanitize(resourceId), sanitize(details));
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replace("\"", "'");
    }
}
