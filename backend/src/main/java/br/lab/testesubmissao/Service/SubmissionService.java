package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Problem;
import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Entity.SubmissionStatus;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Exception.ResourceNotFoundException;
import br.lab.testesubmissao.Repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ✅ REESCRITO: limpo, sem lógica de compilação embutida (isso vai para o Worker).
 *
 * Responsabilidade desta service:
 * - Criar/salvar registros de submissão no banco
 * - Consultar submissões por usuário/problema
 * - Atualizar status/resultado (chamado pelo Worker via fila futuramente)
 * - Deletar submissões
 *
 * A lógica de compilação e execução NÃO fica aqui — fica no módulo Worker,
 * que consumirá mensagens da fila (RabbitMQ) com o submissionId.
 */
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    /**
     * Cria uma nova submissão com status PENDING.
     * O Worker será responsável por processar e atualizar o status.
     */
    public Submission create(Submission submission) {
        submission.setStatus(SubmissionStatus.PENDING);
        return submissionRepository.save(submission);
    }

    /**
     * Busca submissão por ID. Lança exceção se não encontrada.
     */
    public Submission findById(UUID id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submissão não encontrada: " + id));
    }

    /**
     * Lista todas as submissões de um usuário, mais recentes primeiro.
     */
    public List<Submission> findByUser(User user) {
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user);
    }

    /**
     * Lista submissões de um usuário em um problema específico.
     */
    public List<Submission> findByUserAndProblem(User user, Problem problem) {
        return submissionRepository.findByUserAndProblemOrderBySubmittedAtDesc(user, problem);
    }

    /**
     * Lista todas as submissões de um problema (admin/estatísticas).
     */
    public List<Submission> findByProblem(Problem problem) {
        return submissionRepository.findByProblemOrderBySubmittedAtDesc(problem);
    }

    /**
     * Busca submissões por status (ex: PENDING para o Worker processar).
     */
    public List<Submission> findByStatus(SubmissionStatus status) {
        return submissionRepository.findByStatus(status);
    }

    /**
     * Atualiza o status de uma submissão. Chamado pelo Worker após processar.
     */
    public Submission updateStatus(UUID id, SubmissionStatus newStatus, Integer executionTimeMs, Integer memoryUsedKb) {
        Submission submission = findById(id);
        submission.setStatus(newStatus);
        if (executionTimeMs != null) submission.setExecutionTimeMs(executionTimeMs);
        if (memoryUsedKb != null) submission.setMemoryUsedKb(memoryUsedKb);
        return submissionRepository.save(submission);
    }

    /**
     * Verifica se o usuário já resolveu o problema (tem alguma submissão ACCEPTED).
     */
    public boolean userSolvedProblem(User user, Problem problem) {
        return submissionRepository.countByUserAndProblemAndStatus(user, problem, SubmissionStatus.ACCEPTED) > 0;
    }

    /**
     * Remove uma submissão pelo ID.
     */
    public void delete(UUID id) {
        Submission existing = findById(id);
        submissionRepository.delete(existing);
    }
}
