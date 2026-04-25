"""
Docker sandbox for untrusted submissions.

The worker orchestrates containers, but student code only runs inside short-lived
language-specific containers with no network and a single writable workdir mount.
"""

from __future__ import annotations

from dataclasses import dataclass
import logging
import os
from pathlib import Path, PureWindowsPath
import time
from typing import Optional

import docker
from docker.errors import DockerException, ImageNotFound
from docker.types import LogConfig, Mount, Ulimit

log = logging.getLogger("sandbox")


class SandboxUnavailable(RuntimeError):
    """Raised when Docker is unavailable or a sandbox image is missing."""


@dataclass(frozen=True)
class SandboxResult:
    exit_code: Optional[int]
    timed_out: bool
    oom_killed: bool
    duration_ms: int
    stdout: str
    stderr: str


class DockerSandboxRunner:
    def __init__(self):
        self.client = docker.from_env()
        self.data_root = Path(os.getenv("OJ_DATA_ROOT", "/data")).resolve()
        host_data_root = os.getenv("OJ_DOCKER_HOST_DATA_ROOT", "").strip()
        self.host_data_root = self._host_root(host_data_root) if host_data_root else self.data_root
        self.pids_limit = int(os.getenv("OJ_SANDBOX_PIDS_LIMIT", "64"))
        self.cpu_count = float(os.getenv("OJ_SANDBOX_CPUS", "1.0"))
        self.output_limit_kb = int(os.getenv("OJ_SANDBOX_OUTPUT_LIMIT_KB", "1024"))
        self.user = os.getenv("OJ_SANDBOX_USER", "10001:10001")
        self.images = {
            "c": os.getenv("OJ_SANDBOX_IMAGE_C", "oj-sandbox-c:latest"),
            "cpp": os.getenv("OJ_SANDBOX_IMAGE_CPP", "oj-sandbox-cpp:latest"),
            "python": os.getenv("OJ_SANDBOX_IMAGE_PYTHON", "oj-sandbox-python:latest"),
            "java": os.getenv("OJ_SANDBOX_IMAGE_JAVA", "oj-sandbox-java:latest"),
        }

    def run(
        self,
        language: str,
        command: list[str],
        workdir: Path,
        timeout_sec: float,
        memory_limit_kb: int,
    ) -> SandboxResult:
        image = self.images.get(language)
        if not image:
            raise SandboxUnavailable(f"Linguagem sem imagem de sandbox configurada: {language}")

        host_workdir = self._to_host_path(workdir)
        started = time.monotonic()
        container = None
        command_text = " ".join(command)

        try:
            self.client.images.get(image)
        except ImageNotFound as exc:
            raise SandboxUnavailable(
                f"Sandbox image not found: {image}. Run docker compose --profile sandbox-images build."
            ) from exc
        except DockerException as exc:
            raise SandboxUnavailable(f"Docker is unavailable for sandbox execution: {exc}") from exc

        try:
            log.info(
                "event=sandbox_start image=%s language=%s workdir=%s memoryLimitKb=%s "
                "timeoutSec=%s cpus=%s pidsLimit=%s outputLimitKb=%s command=%s",
                image,
                language,
                host_workdir,
                memory_limit_kb,
                timeout_sec,
                self.cpu_count,
                self.pids_limit,
                self.output_limit_kb,
                command_text,
            )
            container = self.client.containers.run(
                image=image,
                command=command,
                detach=True,
                network_disabled=True,
                working_dir="/sandbox",
                user=self.user,
                read_only=True,
                mem_limit=f"{memory_limit_kb}k",
                memswap_limit=f"{memory_limit_kb}k",
                nano_cpus=int(self.cpu_count * 1_000_000_000),
                pids_limit=self.pids_limit,
                cap_drop=["ALL"],
                security_opt=["no-new-privileges:true"],
                tmpfs={
                    "/tmp": "rw,nosuid,nodev,size=64m",
                    "/run": "rw,nosuid,nodev,size=16m",
                },
                mounts=[
                    Mount(
                        target="/sandbox",
                        source=str(host_workdir),
                        type="bind",
                        read_only=False,
                    )
                ],
                ulimits=[
                    Ulimit(
                        name="fsize",
                        soft=self.output_limit_kb * 1024,
                        hard=self.output_limit_kb * 1024,
                    ),
                    Ulimit(name="nproc", soft=self.pids_limit, hard=self.pids_limit),
                ],
                log_config=LogConfig(type=LogConfig.types.JSON, config={"max-size": "1m"}),
                labels={
                    "onlinejudge.sandbox": "true",
                    "onlinejudge.language": language,
                },
            )
            container_id = container.id[:12]

            try:
                wait_result = container.wait(timeout=timeout_sec)
                timed_out = False
            except Exception:
                timed_out = True
                log.warning(
                    "event=sandbox_timeout container=%s image=%s language=%s timeoutSec=%s command=%s",
                    container.id[:12],
                    image,
                    language,
                    timeout_sec,
                    command_text,
                )
                container.kill()
                wait_result = container.wait(timeout=5)

            container.reload()
            inspection = container.attrs
            state = inspection.get("State", {})
            exit_code = wait_result.get("StatusCode")
            oom_killed = bool(state.get("OOMKilled")) or exit_code == 137
            logs = container.logs(stdout=True, stderr=True, tail=200).decode("utf-8", errors="replace")
            duration_ms = int((time.monotonic() - started) * 1000)
            log.info(
                "event=sandbox_finish container=%s image=%s language=%s exitCode=%s "
                "timedOut=%s oomKilled=%s durationMs=%s",
                container_id,
                image,
                language,
                exit_code,
                timed_out,
                oom_killed,
                duration_ms,
            )
            return SandboxResult(
                exit_code=exit_code,
                timed_out=timed_out,
                oom_killed=oom_killed,
                duration_ms=duration_ms,
                stdout="",
                stderr=logs,
            )
        except DockerException as exc:
            raise SandboxUnavailable(f"Failed to run sandbox container: {exc}") from exc
        finally:
            if container is not None:
                try:
                    container.remove(force=True)
                except DockerException:
                    log.warning("event=sandbox_remove_failed container=%s", container.id, exc_info=True)

    def _to_host_path(self, path: Path) -> Path:
        resolved = path.resolve()
        try:
            relative = resolved.relative_to(self.data_root)
        except ValueError as exc:
            raise SandboxUnavailable(f"Workdir is outside OJ_DATA_ROOT: {resolved}") from exc
        host_path = self.host_data_root
        for part in relative.parts:
            host_path = host_path / part
        return host_path

    @staticmethod
    def _host_root(path: str):
        if len(path) >= 2 and path[1] == ":":
            return PureWindowsPath(path)
        return Path(path).resolve()
