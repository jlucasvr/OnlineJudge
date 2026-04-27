package br.lab.testesubmissao.Exception;

import java.util.UUID;

public class SubmissionDispatchException extends RuntimeException {

    private final UUID submissionId;

    public SubmissionDispatchException(String message, UUID submissionId, Throwable cause) {
        super(message, cause);
        this.submissionId = submissionId;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }
}
