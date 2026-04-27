package br.lab.testesubmissao.Dto.submission;

import java.util.UUID;

/**
 * Mensagem publicada na fila RabbitMQ após uma submissão ser criada.
 * O Worker consome essa mensagem e processa a execução.
 */
public record SubmissionQueueMessage(
        UUID submissionId,
        UUID problemId,
        String language,
        String codePath
) {}
