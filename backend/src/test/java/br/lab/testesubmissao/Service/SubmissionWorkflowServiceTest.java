package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.SubmissionStatus;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Exception.SubmissionDispatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionWorkflowServiceTest {

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionCodeStorageService submissionCodeStorageService;

    @Mock
    private SubmissionQueueService submissionQueueService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SubmissionWorkflowService submissionWorkflowService;

    @Test
    void createAndDispatchQueuesSubmissionWhenPublishSucceeds() {
        User user = new User();
        user.setUsername("joao");

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        Submission created = new Submission();
        created.setId(UUID.randomUUID());
        created.setProblem(problem);

        Submission queued = new Submission();
        queued.setId(created.getId());
        queued.setStatus(SubmissionStatus.QUEUED);

        when(submissionCodeStorageService.store("python", "print(1)")).thenReturn("/data/submissions/code.py");
        when(submissionService.create(any(Submission.class))).thenReturn(created);
        when(submissionService.updateStatus(created.getId(), SubmissionStatus.QUEUED, null, null)).thenReturn(queued);

        Submission result = submissionWorkflowService.createAndDispatch(user, problem, "python", "print(1)");

        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionService).create(submissionCaptor.capture());
        assertEquals("python", submissionCaptor.getValue().getLanguage());
        assertEquals("/data/submissions/code.py", submissionCaptor.getValue().getCodePath());
        verify(submissionQueueService).publish(created);
        verify(submissionService).updateStatus(created.getId(), SubmissionStatus.QUEUED, null, null);
        assertEquals(SubmissionStatus.QUEUED, result.getStatus());
    }

    @Test
    void createAndDispatchMarksSubmissionInternalErrorWhenPublishFails() {
        User user = new User();
        user.setUsername("joao");

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        Submission created = new Submission();
        created.setId(UUID.randomUUID());
        created.setProblem(problem);

        Submission failed = new Submission();
        failed.setId(created.getId());
        failed.setStatus(SubmissionStatus.INTERNAL_ERROR);

        when(submissionCodeStorageService.store("c", "int main(){}")).thenReturn("/data/submissions/code.c");
        when(submissionService.create(any(Submission.class))).thenReturn(created);
        when(submissionService.updateStatus(created.getId(), SubmissionStatus.INTERNAL_ERROR, null, null)).thenReturn(failed);
        doThrow(new RuntimeException("rabbit down")).when(submissionQueueService).publish(created);

        assertThrows(SubmissionDispatchException.class,
                () -> submissionWorkflowService.createAndDispatch(user, problem, "c", "int main(){}"));

        verify(submissionService).updateStatus(created.getId(), SubmissionStatus.INTERNAL_ERROR, null, null);
        verify(auditLogService).logSubmission(eq("joao"), eq("dispatch_failed"), eq(created.getId().toString()), any());
    }
}
