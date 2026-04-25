#!/usr/bin/env python3
"""
Online Judge Worker
===================
Consome a fila RabbitMQ 'submissions.queue', executa os casos de teste
e reporta os resultados para a API via HTTP REST.

Fluxo por mensagem:
  1. Recebe {submissionId, problemId, language, codePath}
  2. Busca lista de test cases via GET /internal/problems/{problemId}/test-cases
  3. Atualiza submission → RUNNING
  4. Para cada test case (em ordem):
       a. Compila (se necessário)
       b. Executa com timeout e limite de memória
       c. Compara saída com expected output
       d. POST /internal/verdicts  (result + actual_output se is_sample)
       e. Se falhou → para de executar
  5. Atualiza status final da submission (ACCEPTED / WRONG_ANSWER / TLE / MLE / RE / CE)
"""

import json
import logging
import os
import signal
import sys
import time

import pika

from judge import Judge

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
log = logging.getLogger("worker")

RABBITMQ_HOST     = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT     = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER     = os.getenv("RABBITMQ_USERNAME", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")
QUEUE_NAME        = "submissions.queue"

RETRY_DELAY_SECONDS = 5


def connect_with_retry() -> pika.BlockingConnection:
    """Tenta conectar ao RabbitMQ repetidamente até conseguir."""
    while True:
        try:
            credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
            params = pika.ConnectionParameters(
                host=RABBITMQ_HOST,
                port=RABBITMQ_PORT,
                credentials=credentials,
                heartbeat=600,
                blocked_connection_timeout=300,
            )
            conn = pika.BlockingConnection(params)
            log.info("Conectado ao RabbitMQ em %s:%s", RABBITMQ_HOST, RABBITMQ_PORT)
            return conn
        except Exception as e:
            log.warning("RabbitMQ indisponível (%s). Tentando em %ds...", e, RETRY_DELAY_SECONDS)
            time.sleep(RETRY_DELAY_SECONDS)


def on_message(channel, method, _properties, body):
    """Callback chamado para cada mensagem da fila."""
    try:
        payload = json.loads(body)
        log.info("Mensagem recebida: %s", payload)
        judge = Judge()
        judge.process(payload)
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        log.error("Erro ao processar mensagem: %s", e, exc_info=True)
        # nack sem requeue para evitar loop infinito em mensagens inválidas
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def main():
    connection = connect_with_retry()
    channel = connection.channel()

    # Garante que a fila existe (idempotente)
    channel.queue_declare(queue=QUEUE_NAME, durable=True)

    # Processa uma mensagem por vez (não pega próxima antes de finalizar)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=on_message)

    log.info("Worker aguardando mensagens em '%s'. CTRL+C para encerrar.", QUEUE_NAME)

    def _graceful_shutdown(sig, frame):
        log.info("Sinal %s recebido. Encerrando...", sig)
        channel.stop_consuming()
        connection.close()
        sys.exit(0)

    signal.signal(signal.SIGTERM, _graceful_shutdown)
    signal.signal(signal.SIGINT, _graceful_shutdown)

    channel.start_consuming()


if __name__ == "__main__":
    main()
