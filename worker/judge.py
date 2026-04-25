"""
judge.py
========
Núcleo do julgamento: compila, executa, compara e reporta.
"""

import logging
import os
import shutil
from pathlib import Path
from typing import Optional

import requests
from sandbox import DockerSandboxRunner, SandboxUnavailable

log = logging.getLogger("judge")

# ---------------------------------------------------------------------------
# Configuração via variáveis de ambiente
# ---------------------------------------------------------------------------
API_BASE_URL    = os.getenv("API_BASE_URL", "http://api:8081")
API_USERNAME    = os.getenv("WORKER_API_USERNAME", "worker")
API_PASSWORD    = os.getenv("WORKER_API_PASSWORD", "")
DATA_ROOT       = Path(os.getenv("OJ_DATA_ROOT", "/data"))

ACTUAL_OUTPUT_MAX_CHARS = 4096

# Resultados possíveis no veredicto (campo result da tabela verdict)
ACCEPTED        = "ACCEPTED"
WRONG_ANSWER    = "WRONG_ANSWER"
TLE             = "TLE"
MLE             = "MLE"
RUNTIME_ERROR   = "RE"
COMPILE_ERROR   = "CE"
INTERNAL_ERROR  = "INTERNAL_ERROR"

# Status da submissão (alinhados com o enum SubmissionStatus do banco)
STATUS_RUNNING           = "RUNNING"
STATUS_ACCEPTED          = "ACCEPTED"
STATUS_WRONG_ANSWER      = "WRONG_ANSWER"
STATUS_TIME_LIMIT        = "TIME_LIMIT"
STATUS_MEMORY_LIMIT      = "MEMORY_LIMIT"
STATUS_RUNTIME_ERROR     = "RUNTIME_ERROR"
STATUS_COMPILATION_ERROR = "COMPILATION_ERROR"
STATUS_INTERNAL_ERROR    = "INTERNAL_ERROR"

# Mapeamento: resultado do veredicto → status da submissão
VERDICT_TO_STATUS = {
    ACCEPTED:      STATUS_ACCEPTED,
    WRONG_ANSWER:  STATUS_WRONG_ANSWER,
    TLE:           STATUS_TIME_LIMIT,
    MLE:           STATUS_MEMORY_LIMIT,
    RUNTIME_ERROR: STATUS_RUNTIME_ERROR,
    COMPILE_ERROR: STATUS_COMPILATION_ERROR,
    INTERNAL_ERROR: STATUS_INTERNAL_ERROR,
}


class ApiClient:
    """Cliente HTTP para a API do Online Judge."""

    def __init__(self):
        self._session = requests.Session()
        self._token: Optional[str] = None

    def _ensure_token(self):
        if self._token:
            return
        resp = self._session.post(
            f"{API_BASE_URL}/auth/login",
            json={"username": API_USERNAME, "password": API_PASSWORD},
            timeout=10,
        )
        resp.raise_for_status()
        self._token = resp.json()["token"]
        self._session.headers.update({"Authorization": f"Bearer {self._token}"})

    def _request(self, method: str, path: str, **kwargs):
        self._ensure_token()
        resp = self._session.request(method, f"{API_BASE_URL}{path}", timeout=15, **kwargs)
        if resp.status_code == 401:
            # Token expirado → renova e tenta uma vez mais
            self._token = None
            self._ensure_token()
            resp = self._session.request(method, f"{API_BASE_URL}{path}", timeout=15, **kwargs)
        resp.raise_for_status()
        return resp.json()

    def get_test_cases(self, problem_id: str) -> list:
        return self._request("GET", f"/internal/problems/{problem_id}/test-cases")

    def update_submission_status(self, submission_id: str, status: str,
                                  execution_time_ms: Optional[int] = None,
                                  memory_used_kb: Optional[int] = None):
        body = {"status": status}
        if execution_time_ms is not None:
            body["executionTimeMs"] = execution_time_ms
        if memory_used_kb is not None:
            body["memoryUsedKb"] = memory_used_kb
        self._request("PATCH", f"/internal/submissions/{submission_id}/status", json=body)

    def post_verdict(self, submission_id: str, test_case_id: str, result: str,
                     execution_time_ms: Optional[int], memory_used_kb: Optional[int],
                     checker_output: str, actual_output: Optional[str]):
        body = {
            "submissionId": submission_id,
            "testCaseId": test_case_id,
            "result": result,
            "executionTimeMs": execution_time_ms,
            "memoryUsedKb": memory_used_kb,
            "checkerOutput": checker_output,
            "actualOutput": actual_output,
        }
        self._request("POST", "/internal/verdicts", json=body)


class Judge:
    """Processa uma submissão completa."""

    def __init__(self):
        self.api = ApiClient()
        self.sandbox: Optional[DockerSandboxRunner] = None

    def _sandbox(self) -> DockerSandboxRunner:
        if self.sandbox is None:
            self.sandbox = DockerSandboxRunner()
        return self.sandbox

    def process(self, payload: dict):
        submission_id = payload["submissionId"]
        problem_id    = payload["problemId"]
        language      = payload["language"]
        code_path     = payload["codePath"]

        log.info("[%s] Iniciando julgamento | problema=%s | lang=%s",
                 submission_id, problem_id, language)

        # Atualiza para RUNNING
        self.api.update_submission_status(submission_id, "RUNNING")

        # Busca test cases
        try:
            test_cases = self.api.get_test_cases(problem_id)
        except Exception as e:
            log.error("[%s] Falha ao buscar test cases: %s", submission_id, e)
            self.api.update_submission_status(submission_id, STATUS_INTERNAL_ERROR)
            return

        if not test_cases:
            log.warning("[%s] Nenhum test case encontrado.", submission_id)
            self.api.update_submission_status(submission_id, "ACCEPTED")
            return

        # Diretório temporário de execução: data/temp/exec/{submissionId}/
        exec_dir = DATA_ROOT / "temp" / "exec" / submission_id
        exec_dir.mkdir(parents=True, exist_ok=True)
        # O container de sandbox roda como uid 1000 e só recebe este diretório.
        os.chmod(exec_dir, 0o777)

        try:
            final_verdict = self._run_all(
                submission_id, language, code_path, test_cases, exec_dir
            )
        finally:
            # Limpa diretório temporário após julgar
            shutil.rmtree(exec_dir, ignore_errors=True)

        final_status = VERDICT_TO_STATUS.get(final_verdict, STATUS_INTERNAL_ERROR)
        log.info("[%s] Veredicto final: %s → Status: %s", submission_id, final_verdict, final_status)
        self.api.update_submission_status(submission_id, final_status)

    # ------------------------------------------------------------------
    # Lógica interna
    # ------------------------------------------------------------------

    def _run_all(self, submission_id: str, language: str, code_path: str,
                 test_cases: list, exec_dir: Path) -> str:
        """
        Compila (se necessário) e executa todos os test cases em ordem.
        Para na primeira falha. Retorna o status final da submissão.
        """
        # Copia o código para o diretório de execução
        src_file = exec_dir / self._src_filename(language)
        shutil.copy2(code_path, src_file)

        # Compilação (C / C++)
        binary_path = exec_dir / "main"
        if language in ("c", "cpp", "java"):
            try:
                ce_output = self._compile(language, src_file, binary_path, submission_id)
            except SandboxUnavailable as exc:
                log.error("[%s] Sandbox indisponível na compilação: %s", submission_id, exc)
                return INTERNAL_ERROR
            if ce_output is not None:
                # Posta CE no primeiro test case para o usuário ter feedback
                first_tc = test_cases[0]
                self.api.post_verdict(
                    submission_id=submission_id,
                    test_case_id=first_tc["id"],
                    result=COMPILE_ERROR,
                    execution_time_ms=None,
                    memory_used_kb=None,
                    checker_output=ce_output[:2048],
                    actual_output=None,
                )
                return COMPILE_ERROR

        max_time_ms  = 0
        max_mem_kb   = 0
        final_result = ACCEPTED

        for tc in test_cases:
            tc_id      = tc["id"]
            is_sample  = tc.get("isSample", False)
            input_path = Path(tc["inputPath"])
            exp_path   = Path(tc["outputPath"])

            try:
                result, time_ms, mem_kb, actual_out, checker_msg = self._run_one(
                    language=language,
                    binary_path=binary_path,
                    src_file=src_file,
                    exec_dir=exec_dir,
                    input_path=input_path,
                    expected_path=exp_path,
                    time_limit_ms=tc.get("timeLimitMs", 2000),
                    memory_limit_kb=tc.get("memoryLimitKb", 262144),
                )
            except SandboxUnavailable as exc:
                log.error("[%s] Sandbox indisponível na execução: %s", submission_id, exc)
                return INTERNAL_ERROR

            # actual_output só enviado para samples
            actual_to_send = actual_out if is_sample else None

            self.api.post_verdict(
                submission_id=submission_id,
                test_case_id=tc_id,
                result=result,
                execution_time_ms=time_ms,
                memory_used_kb=mem_kb,
                checker_output=checker_msg,
                actual_output=actual_to_send,
            )

            if time_ms and time_ms > max_time_ms:
                max_time_ms = time_ms
            if mem_kb and mem_kb > max_mem_kb:
                max_mem_kb = mem_kb

            if result != ACCEPTED:
                final_result = result
                break  # Para ao primeiro erro

        return final_result

    def _compile(self, language: str, src: Path, binary: Path,
                 submission_id: str) -> Optional[str]:
        """
        Compila o código. Retorna None em sucesso, ou a mensagem de erro.
        Salva erro em data/compile-errors/{submissionId}.txt
        """
        if language == "c":
            cmd = ["gcc", "/sandbox/main.c", "-o", "/sandbox/main", "-lm", "-O2", "-std=c17"]
        elif language == "cpp":
            cmd = ["g++", "/sandbox/main.cpp", "-o", "/sandbox/main", "-lm", "-O2", "-std=c++17"]
        elif language == "java":
            cmd = ["javac", "-encoding", "UTF-8", "-d", "/sandbox", "/sandbox/Main.java"]
        else:
            return f"Linguagem não suportada para compilação: {language}"

        result = self._sandbox().run(
            language=language,
            command=cmd,
            workdir=src.parent,
            timeout_sec=30,
            memory_limit_kb=524288,
        )

        if result.timed_out:
            return "Compilação excedeu o tempo limite."

        if result.exit_code != 0:
            error_msg = result.stderr or result.stdout or f"Compilador retornou exit code {result.exit_code}."
            # Persiste o erro de compilação
            ce_dir = DATA_ROOT / "compile-errors"
            ce_dir.mkdir(parents=True, exist_ok=True)
            (ce_dir / f"{submission_id}.txt").write_text(error_msg, encoding="utf-8")
            log.warning("[%s] Compile Error:\n%s", submission_id, error_msg[:500])
            return error_msg

        return None  # sucesso

    def _run_one(self, language: str, binary_path: Path, src_file: Path,
                 exec_dir: Path, input_path: Path, expected_path: Path,
                 time_limit_ms: int, memory_limit_kb: int):
        """
        Executa um test case. Retorna (result, time_ms, mem_kb, actual_output, checker_msg).
        """
        input_dest  = exec_dir / "input.txt"
        actual_dest = exec_dir / "actual_output.txt"
        stderr_dest = exec_dir / "stderr.txt"

        # Copia input do test case para o diretório de execução
        try:
            shutil.copy2(input_path, input_dest)
        except FileNotFoundError:
            return RUNTIME_ERROR, None, None, None, f"Input não encontrado: {input_path}"

        # Monta comando de execução por linguagem dentro do container descartável.
        if language in ("c", "cpp"):
            program_cmd = "./main"
        elif language == "python":
            program_cmd = "python3 /sandbox/main.py"
        elif language == "java":
            # Assume que a classe se chama Main
            program_cmd = "java -cp /sandbox Main"
        else:
            return RUNTIME_ERROR, None, None, None, f"Linguagem não suportada: {language}"

        time_limit_sec = (time_limit_ms / 1000) + 1  # margem de 1s

        result = self._sandbox().run(
            language=language,
            command=[
                "/bin/sh",
                "-lc",
                f"{program_cmd} < /sandbox/input.txt > /sandbox/actual_output.txt 2> /sandbox/stderr.txt",
            ],
            workdir=exec_dir,
            timeout_sec=time_limit_sec,
            memory_limit_kb=memory_limit_kb,
        )

        elapsed_ms = result.duration_ms

        if result.timed_out:
            return TLE, time_limit_ms, None, None, "Tempo limite excedido."

        if result.oom_killed:
            actual_content = _read_truncated(actual_dest, ACTUAL_OUTPUT_MAX_CHARS)
            return MLE, int(elapsed_ms), None, actual_content, "Limite de memória excedido."

        # Verifica código de saída
        if result.exit_code != 0:
            stderr_content = _read_truncated(stderr_dest, 512)
            actual_content = _read_truncated(actual_dest, ACTUAL_OUTPUT_MAX_CHARS)
            return RUNTIME_ERROR, int(elapsed_ms), None, actual_content, \
                   f"Exit code {result.exit_code}. Stderr: {stderr_content}"

        # Lê saída real e esperada para comparação
        actual_output   = _read_file(actual_dest)
        expected_output = _read_file(expected_path)

        result, checker_msg = _compare(actual_output, expected_output)

        actual_truncated = _truncate(actual_output, ACTUAL_OUTPUT_MAX_CHARS)

        return result, int(elapsed_ms), None, actual_truncated, checker_msg

    @staticmethod
    def _src_filename(language: str) -> str:
        ext = {"c": "c", "cpp": "cpp", "java": "java", "python": "py"}.get(language, "txt")
        if language == "java":
            return "Main.java"
        return f"main.{ext}"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _read_file(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="replace")
    except FileNotFoundError:
        return ""


def _read_truncated(path: Path, max_chars: int) -> str:
    return _truncate(_read_file(path), max_chars)


def _truncate(text: str, max_chars: int) -> str:
    if len(text) <= max_chars:
        return text
    return text[:max_chars] + "\n... [saída truncada]"


def _compare(actual: str, expected: str) -> tuple[str, str]:
    """
    Comparador token-based (ignora espaços e quebras de linha no final).
    Retorna (result, checker_message).
    """
    actual_norm   = actual.strip()
    expected_norm = expected.strip()

    if actual_norm == expected_norm:
        return ACCEPTED, "OK"

    # Comparação linha a linha para mensagem útil
    actual_lines   = actual_norm.splitlines()
    expected_lines = expected_norm.splitlines()

    for i, (a, e) in enumerate(zip(actual_lines, expected_lines), start=1):
        if a.rstrip() != e.rstrip():
            return WRONG_ANSWER, (
                f"Diferença na linha {i}. "
                f"Esperado: {repr(e[:80])} | Obtido: {repr(a[:80])}"
            )

    if len(actual_lines) != len(expected_lines):
        return WRONG_ANSWER, (
            f"Número de linhas diferente. "
            f"Esperado: {len(expected_lines)} | Obtido: {len(actual_lines)}"
        )

    return WRONG_ANSWER, "Saída incorreta (diferença de whitespace/formatação)."
