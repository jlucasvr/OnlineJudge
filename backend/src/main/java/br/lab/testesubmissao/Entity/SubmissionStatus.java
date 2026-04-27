package br.lab.testesubmissao.Entity;

public enum SubmissionStatus {
    PENDING,
    QUEUED,
    RUNNING,
    ACCEPTED,
    WRONG_ANSWER,
    TIME_LIMIT,
    MEMORY_LIMIT,
    RUNTIME_ERROR,
    COMPILATION_ERROR,
    INTERNAL_ERROR
}
