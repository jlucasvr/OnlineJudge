package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Config.RabbitMQConfig;
import br.lab.testesubmissao.Dto.submission.SubmissionQueueMessage;
import br.lab.testesubmissao.Entity.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SubmissionQueueService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionQueueService.class);

    private final RabbitTemplate rabbitTemplate;

    public SubmissionQueueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica a submissão na fila para o Worker processar.
     * A mensagem contém apenas o necessário: IDs e localização do código.
     */
    public void publish(Submission submission) {
        SubmissionQueueMessage message = new SubmissionQueueMessage(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getLanguage(),
                submission.getCodePath()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.SUBMISSION_QUEUE, message);
        log.info("[Queue] Submissão {} publicada na fila.", submission.getId());
    }
}
