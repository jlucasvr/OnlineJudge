# Online Judge — Backend + Worker

## Arquitetura

```
┌─────────────┐    POST /submissions     ┌───────────────┐
│   Frontend  │ ──────────────────────▶ │   API (Java)  │
└─────────────┘                          └───────┬───────┘
                                                 │ publica na fila
                                                 ▼
                                        ┌────────────────┐
                                        │   RabbitMQ     │
                                        │ submissions.   │
                                        │    queue       │
                                        └───────┬────────┘
                                                │ consome
                                                ▼
                                        ┌───────────────┐
                                        │ Worker (Py)   │
                                        │  compila &    │
                                        │  executa      │
                                        └───────┬───────┘
                                                │ POST /internal/verdicts
                                                │ PATCH /submissions/{id}/status
                                                ▼
                                        ┌───────────────┐
                                        │   API (Java)  │
                                        └───────┬───────┘
                                                │
                                                ▼
                                        ┌───────────────┐
                                        │  PostgreSQL   │
                                        └───────────────┘
```

## Diretório Compartilhado (`OJ_HOST_DATA_ROOT`)

Montado em `/data` tanto na API quanto no Worker. Também é usado pelo Worker
para montar apenas `temp/exec/{submissionId}` nos containers descartáveis de
sandbox. Use um caminho absoluto do host em `.env`:

```bash
OJ_HOST_DATA_ROOT=/opt/onlinejudge/data
OJ_DOCKER_HOST_DATA_ROOT=/opt/onlinejudge/data
```

```
/data/
├── submissions/
│   └── {year}/
│       └── {month}/
│           └── {uuid}.c          ← código salvo pela API, lido pelo Worker
├── testcases/
│   └── {problemId}/
│       ├── inputs/
│       │   ├── 1                 ← input do caso 1
│       │   └── 2
│       └── outputs/
│           ├── 1                 ← saída esperada do caso 1
│           └── 2
├── compile-errors/
│   └── {submissionId}.txt        ← log de erro de compilação
└── temp/
    └── exec/
        └── {submissionId}/       ← diretório limpo após cada julgamento
            ├── main.c
            ├── main              ← binário compilado
            ├── input.txt
            ├── actual_output.txt
            └── stderr.txt
```

## Subir o ambiente

Execute os comandos a partir da pasta `infra/`.

```bash
# 1. Entre na pasta de infraestrutura
cd infra

# 2. Crie seu arquivo de ambiente local
cp .env.example .env

# 3. Construa as imagens de sandbox por linguagem
docker compose --profile sandbox-images build

# 4. Suba tudo
docker compose up --build
```

Observações:
- A API sobe em `http://localhost:8081`
- O compose usa o perfil Spring `dev`
- Se `FRONTEND_URL` não for informado, a raiz `/` redireciona para o Swagger UI
- A conta de serviço do Worker é criada automaticamente pela API no boot quando `OJ_BOOTSTRAP_WORKER_ENABLED=true`
- O script `worker_service_account.sql` fica como fallback manual
- O Worker precisa de acesso ao Docker socket para criar containers de sandbox
- Cada execução roda sem rede, sem capabilities, com `no-new-privileges`,
  filesystem raiz read-only, limite de processos, CPU, memória e tamanho de
  arquivo de saída

## Cadastrar Test Cases

Os arquivos de input/output devem ser colocados no volume em:
```
/data/testcases/{problemId}/inputs/{orderIndex}
/data/testcases/{problemId}/outputs/{orderIndex}
```

Ao cadastrar o test case via API, informe esses paths:
```json
POST /problems/{problemId}/test-cases
{
  "orderIndex": 1,
  "inputPath": "/data/testcases/{problemId}/inputs/1",
  "outputPath": "/data/testcases/{problemId}/outputs/1",
  "points": 100,
  "isSample": true
}
```

## Fluxo de Julgamento

1. Usuário faz `POST /submissions` com código-fonte
2. API salva o código em `/data/submissions/{year}/{month}/{uuid}.c`
3. API publica `{submissionId, problemId, language, codePath}` na fila RabbitMQ
4. Worker consome a mensagem:
   - Atualiza status → `RUNNING`
   - Busca test cases via `GET /internal/problems/{problemId}/test-cases`
   - Para cada test case (em ordem):
     - Compila em container descartável, se a linguagem exigir
     - Executa em container descartável com limite de tempo, memória, CPU,
       processos, filesystem e rede bloqueada
     - Compara saída com expected output
     - `POST /internal/verdicts` com resultado
       - `actual_output` preenchido **somente** se `is_sample = true`
     - Para se falhou
5. `PATCH /submissions/{id}/status` com status final

## Endpoints Internos (Worker)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/internal/problems/{problemId}/test-cases` | Lista test cases para o Worker |
| `POST` | `/internal/verdicts` | Salva veredicto de um test case |

Autenticação: Bearer JWT da conta `worker` (ROLE_ADMIN).

## Sandbox de Execução

Imagens base:

| Linguagem | Imagem |
|-----------|--------|
| C | `oj-sandbox-c:latest` |
| C++ | `oj-sandbox-cpp:latest` |
| Python | `oj-sandbox-python:latest` |
| Java | `oj-sandbox-java:latest` |

O container de sandbox recebe somente `/sandbox`, mapeado para o diretório
efêmero da submissão. Test cases e demais submissões não são montados no
container; o Worker copia apenas o input necessário e lê o output esperado fora
da sandbox.

Variáveis úteis:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `OJ_SANDBOX_CPUS` | `1.0` | cota de CPU por execução |
| `OJ_SANDBOX_PIDS_LIMIT` | `64` | máximo de processos/threads |
| `OJ_SANDBOX_OUTPUT_LIMIT_KB` | `1024` | limite de arquivo para stdout/stderr |
| `OJ_SANDBOX_USER` | `10001:10001` | usuário sem privilégios dentro da sandbox |
| `OJ_DOCKER_HOST_DATA_ROOT` | `/tmp/onlinejudge-data` | caminho do host para `/data` |

## Testes Maliciosos Automatizados

Execute a partir da raiz do repositório, depois de construir as imagens de
sandbox:

```bash
docker compose -f infra/docker-compose.yml --profile sandbox-images build
docker build -t oj-worker-sandbox-tests worker
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/.oj-data:/data" \
  -e OJ_DATA_ROOT=/data \
  -e OJ_DOCKER_HOST_DATA_ROOT="$PWD/.oj-data" \
  -e OJ_SANDBOX_PIDS_LIMIT=32 \
  -e OJ_SANDBOX_OUTPUT_LIMIT_KB=1024 \
  oj-worker-sandbox-tests \
  python -m unittest tests.test_sandbox_security -v
```

No Windows/PowerShell, use o caminho absoluto no formato aceito pelo Docker:

```powershell
docker run --rm `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -v "C:/Users/João/Documents/Projetos/OnlineJudge/OnlineJudge/.oj-data:/data" `
  -e OJ_DATA_ROOT=/data `
  -e "OJ_DOCKER_HOST_DATA_ROOT=C:/Users/João/Documents/Projetos/OnlineJudge/OnlineJudge/.oj-data" `
  -e OJ_SANDBOX_PIDS_LIMIT=32 `
  -e OJ_SANDBOX_OUTPUT_LIMIT_KB=1024 `
  oj-worker-sandbox-tests `
  python -m unittest tests.test_sandbox_security -v
```

A suíte cobre:

- tentativa de rede em Python, C, C++ e Java;
- leitura de dados do host fora do diretório montado em Python, C, C++ e Java;
- escrita fora de `/sandbox` em Python, C, C++ e Java;
- consumo excessivo de memória em Python, C, C++ e Java;
- consumo excessivo de disco/stdout;
- fork bomb/processos em excesso.
- processo filho persistente após o encerramento da submissão.

## Teste Ponta a Ponta da Sandbox

O script `infra/e2e_sandbox_test.ps1` valida o fluxo real:

```
API -> RabbitMQ -> Worker -> container de sandbox -> API -> PostgreSQL
```

Ele cria um usuário de submissão, cria um problema, grava arquivos de test case
em `.oj-data`, cadastra o test case, submete códigos e espera os status finais.

Execute a partir da raiz do repositório:

```powershell
docker compose -f infra\docker-compose.yml --profile sandbox-images build
docker compose -f infra\docker-compose.yml up -d postgres rabbitmq api worker
.\infra\e2e_sandbox_test.ps1 -TimeoutSeconds 180
```

O script lê `infra/.env` para usar:

- `WORKER_API_USERNAME`
- `WORKER_API_PASSWORD`
- `OJ_HOST_DATA_ROOT`

Também é possível sobrescrever por parâmetro:

```powershell
.\infra\e2e_sandbox_test.ps1 `
  -ApiBaseUrl http://localhost:8081 `
  -DataRoot "C:/Users/João/Documents/Projetos/OnlineJudge/OnlineJudge/.oj-data" `
  -WorkerUsername worker `
  -WorkerPassword "sua-senha" `
  -TimeoutSeconds 180
```

Casos validados pelo E2E:

| Caso | Resultado esperado | O que valida |
|------|--------------------|--------------|
| `accepted_python` | `ACCEPTED` | fluxo feliz completo |
| `wrong_answer_python` | `WRONG_ANSWER` | comparação de saída |
| `time_limit_python` | `TIME_LIMIT` | limite real de tempo |
| `network_blocked_python` | `ACCEPTED` | rede bloqueada na sandbox |
| `host_data_not_mounted_python` | `ACCEPTED` | `/data` não é visível no container do aluno |
| `memory_pressure_python` | `MEMORY_LIMIT` | limite real de memória |

Saída esperada:

```text
E2E sandbox concluido com sucesso.

Case                         Status        Verdict
accepted_python              ACCEPTED      ACCEPTED
wrong_answer_python          WRONG_ANSWER  WRONG_ANSWER
time_limit_python            TIME_LIMIT    TLE
network_blocked_python       ACCEPTED      ACCEPTED
host_data_not_mounted_python ACCEPTED      ACCEPTED
memory_pressure_python       MEMORY_LIMIT  MLE
```

Para acompanhar a execução em tempo real:

```powershell
docker logs -f oj-worker
docker logs -f oj-api
```

Para parar o ambiente após o teste:

```powershell
docker compose -f infra\docker-compose.yml down
```

## actual_output

- Só é salvo no banco quando o test case tem `is_sample = true`
- Truncado em 4096 caracteres se necessário
- Visível ao usuário via `GET /submissions/{id}/verdicts`

## Migrations Flyway

| Versão | Descrição |
|--------|-----------|
| V1 | Schema inicial |
| V2 | Normaliza status da submissão |
| V3 | Padroniza role e difficulty como enums |
| V4 | Adiciona `actual_output` na tabela `verdict` |
