package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.SubmissionStatus;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Exception.SubmissionDispatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SubmissionWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionWorkflowService.class);

    private final SubmissionService submissionService;
    private final SubmissionCodeStorageService submissionCodeStorageService;
    private final SubmissionQueueService submissionQueueService;
    private final AuditLogService auditLogService;

    public SubmissionWorkflowService(SubmissionService submissionService,
                                     SubmissionCodeStorageService submissionCodeStorageService,
                                     SubmissionQueueService submissionQueueService,
                                     AuditLogService auditLogService) {
        this.submissionService = submissionService;
        this.submissionCodeStorageService = submissionCodeStorageService;
        this.submissionQueueService = submissionQueueService;
        this.auditLogService = auditLogService;
    }

    public Submission createAndDispatch(User user, Problem problem, String language, String sourceCode) {
        String normalizedLanguage = language.toLowerCase(Locale.ROOT);

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(normalizedLanguage);
        submission.setCodePath(submissionCodeStorageService.store(normalizedLanguage, sourceCode));

        Submission saved = submissionService.create(submission);
        auditLogService.logSubmission(user.getUsername(), "created", saved.getId().toString(),
                "problemId=" + problem.getId() + " language=" + normalizedLanguage);

        try {
            submissionQueueService.publish(saved);
            Submission queued = submissionService.updateStatus(saved.getId(), SubmissionStatus.QUEUED, null, null);
            auditLogService.logSubmission(user.getUsername(), "queued", queued.getId().toString(),
                    "problemId=" + problem.getId());
            return queued;
        } catch (Exception ex) {
            Submission failed = submissionService.updateStatus(saved.getId(), SubmissionStatus.INTERNAL_ERROR, null, null);
            auditLogService.logSubmission(user.getUsername(), "dispatch_failed", failed.getId().toString(),
                    "problemId=" + problem.getId());
            log.error("Falha ao publicar submissão {} na fila.", failed.getId(), ex);
            throw new SubmissionDispatchException(
                    "Submissão recebida, mas não foi possível enviá-la para processamento.",
                    failed.getId(),
                    ex
            );
        }
    }
}
