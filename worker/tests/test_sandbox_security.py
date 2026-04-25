import os
from pathlib import Path
import shutil
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from sandbox import DockerSandboxRunner, SandboxUnavailable


REPO_ROOT = Path(__file__).resolve().parents[2]
DATA_ROOT = Path(os.getenv("OJ_DATA_ROOT", REPO_ROOT / ".oj-data")).resolve()


class SandboxSecurityTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        os.environ.setdefault("OJ_DATA_ROOT", str(DATA_ROOT))
        os.environ.setdefault("OJ_DOCKER_HOST_DATA_ROOT", str(DATA_ROOT))
        os.environ.setdefault("OJ_SANDBOX_PIDS_LIMIT", "32")
        os.environ.setdefault("OJ_SANDBOX_OUTPUT_LIMIT_KB", "1024")
        os.environ.setdefault("OJ_SANDBOX_CPUS", "1.0")

        cls.root = DATA_ROOT / "temp" / "sandbox-security-tests"
        cls.root.mkdir(parents=True, exist_ok=True)
        cls.runner = DockerSandboxRunner()

        try:
            cls.runner.client.ping()
        except Exception as exc:
            raise unittest.SkipTest(f"Docker unavailable: {exc}") from exc

    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(cls.root, ignore_errors=True)
        secret_path = DATA_ROOT / "testcases" / "sandbox-secret.txt"
        secret_path.unlink(missing_ok=True)

    def setUp(self):
        self.workdir = self.root / self._testMethodName
        shutil.rmtree(self.workdir, ignore_errors=True)
        self.workdir.mkdir(parents=True, exist_ok=True)
        os.chmod(self.workdir, 0o777)

    def tearDown(self):
        shutil.rmtree(self.workdir, ignore_errors=True)

    def write_file(self, name: str, source: str):
        path = self.workdir / name
        path.write_text(source.strip() + "\n", encoding="utf-8")
        os.chmod(path, 0o666)
        return path

    def run_sandbox(self, language: str, command: list[str], timeout_sec=5, memory_limit_kb=65536):
        try:
            return self.runner.run(
                language=language,
                command=command,
                workdir=self.workdir,
                timeout_sec=timeout_sec,
                memory_limit_kb=memory_limit_kb,
            )
        except SandboxUnavailable as exc:
            self.fail(str(exc))

    def compile_c(self, source: str, compiler="gcc", std="c17"):
        self.write_file("main.c", source)
        result = self.run_sandbox(
            "c",
            [compiler, "/sandbox/main.c", "-o", "/sandbox/main", "-O2", f"-std={std}"],
            timeout_sec=10,
            memory_limit_kb=131072,
        )
        self.assertEqual(0, result.exit_code, result.stderr)

    def compile_cpp(self, source: str):
        self.write_file("main.cpp", source)
        result = self.run_sandbox(
            "cpp",
            ["g++", "/sandbox/main.cpp", "-o", "/sandbox/main", "-O2", "-std=c++17"],
            timeout_sec=10,
            memory_limit_kb=131072,
        )
        self.assertEqual(0, result.exit_code, result.stderr)

    def compile_java(self, source: str):
        self.write_file("Main.java", source)
        result = self.run_sandbox(
            "java",
            ["javac", "-d", "/sandbox", "/sandbox/Main.java"],
            timeout_sec=15,
            memory_limit_kb=196608,
        )
        self.assertEqual(0, result.exit_code, result.stderr)

    def malicious_programs(self):
        return [
            (
                "python",
                lambda source: self.write_file("main.py", source),
                ["python3", "/sandbox/main.py"],
            ),
            (
                "c",
                self.compile_c,
                ["/sandbox/main"],
            ),
            (
                "cpp",
                self.compile_cpp,
                ["/sandbox/main"],
            ),
            (
                "java",
                self.compile_java,
                ["java", "-cp", "/sandbox", "Main"],
            ),
        ]

    def assert_program_result(self, language, source, command, expected_marker, timeout_sec=5, memory_limit_kb=65536):
        result = self.run_sandbox(language, command, timeout_sec=timeout_sec, memory_limit_kb=memory_limit_kb)

        self.assertEqual(0, result.exit_code, f"{language}: {result.stderr}")
        self.assertIn(expected_marker, result.stderr, f"{language}: {result.stderr}")
        return result

    def test_network_access_is_blocked_for_all_languages(self):
        sources = {
            "python": """
import socket
import sys

sock = socket.socket()
sock.settimeout(2)
try:
    sock.connect(("1.1.1.1", 80))
except OSError:
    print("NETWORK_BLOCKED")
    sys.exit(0)

print("NETWORK_OPEN")
sys.exit(42)
""",
            "c": """
#include <arpa/inet.h>
#include <stdio.h>
#include <sys/socket.h>
#include <unistd.h>

int main(void) {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr_in addr = {0};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(80);
    inet_pton(AF_INET, "1.1.1.1", &addr.sin_addr);
    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        puts("NETWORK_BLOCKED");
        return 0;
    }
    close(sock);
    puts("NETWORK_OPEN");
    return 42;
}
""",
            "cpp": """
#include <arpa/inet.h>
#include <iostream>
#include <sys/socket.h>
#include <unistd.h>

int main() {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(80);
    inet_pton(AF_INET, "1.1.1.1", &addr.sin_addr);
    if (connect(sock, reinterpret_cast<sockaddr *>(&addr), sizeof(addr)) < 0) {
        std::cout << "NETWORK_BLOCKED\\n";
        return 0;
    }
    close(sock);
    std::cout << "NETWORK_OPEN\\n";
    return 42;
}
""",
            "java": """
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("1.1.1.1", 80), 1000);
        } catch (Exception expected) {
            System.out.println("NETWORK_BLOCKED");
            return;
        }
        System.out.println("NETWORK_OPEN");
        System.exit(42);
    }
}
""",
        }

        for language, prepare, command in self.malicious_programs():
            with self.subTest(language=language):
                prepare(sources[language])
                result = self.assert_program_result(language, sources[language], command, "NETWORK_BLOCKED")
                self.assertNotIn("NETWORK_OPEN", result.stderr)

    def test_host_data_root_is_not_mounted_for_all_languages(self):
        secret = "sandbox-secret-host-data"
        secret_path = DATA_ROOT / "testcases" / "sandbox-secret.txt"
        secret_path.parent.mkdir(parents=True, exist_ok=True)
        secret_path.write_text(secret, encoding="utf-8")

        sources = {
            "python": """
from pathlib import Path
import sys

for path in ("/data/testcases/sandbox-secret.txt", "/sandbox/../testcases/sandbox-secret.txt"):
    try:
        content = Path(path).read_text(encoding="utf-8")
    except Exception:
        continue
    print("HOST_DATA_READ", path, content)
    sys.exit(42)

print("HOST_DATA_BLOCKED")
""",
            "c": """
#include <stdio.h>

int main(void) {
    const char *paths[] = {"/data/testcases/sandbox-secret.txt", "/sandbox/../testcases/sandbox-secret.txt"};
    for (int i = 0; i < 2; i++) {
        FILE *file = fopen(paths[i], "r");
        if (file) {
            char buffer[128] = {0};
            fgets(buffer, sizeof(buffer), file);
            fclose(file);
            printf("HOST_DATA_READ %s %s\\n", paths[i], buffer);
            return 42;
        }
    }
    puts("HOST_DATA_BLOCKED");
    return 0;
}
""",
            "cpp": """
#include <fstream>
#include <iostream>
#include <string>

int main() {
    const char *paths[] = {"/data/testcases/sandbox-secret.txt", "/sandbox/../testcases/sandbox-secret.txt"};
    for (const char *path : paths) {
        std::ifstream file(path);
        if (file) {
            std::string content;
            std::getline(file, content);
            std::cout << "HOST_DATA_READ " << path << " " << content << "\\n";
            return 42;
        }
    }
    std::cout << "HOST_DATA_BLOCKED\\n";
    return 0;
}
""",
            "java": """
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        for (String path : new String[] {"/data/testcases/sandbox-secret.txt", "/sandbox/../testcases/sandbox-secret.txt"}) {
            try {
                String content = Files.readString(Path.of(path));
                System.out.println("HOST_DATA_READ " + path + " " + content);
                System.exit(42);
            } catch (Exception expected) {
            }
        }
        System.out.println("HOST_DATA_BLOCKED");
    }
}
""",
        }

        for language, prepare, command in self.malicious_programs():
            with self.subTest(language=language):
                prepare(sources[language])
                result = self.assert_program_result(language, sources[language], command, "HOST_DATA_BLOCKED")
                self.assertNotIn(secret, result.stderr)

    def test_write_outside_sandbox_is_blocked_for_all_languages(self):
        sources = {
            "python": """
from pathlib import Path
import sys

try:
    Path("/outside-write.txt").write_text("x", encoding="utf-8")
except OSError:
    print("WRITE_BLOCKED")
    sys.exit(0)

print("WRITE_ALLOWED")
sys.exit(42)
""",
            "c": """
#include <stdio.h>

int main(void) {
    FILE *file = fopen("/outside-write.txt", "w");
    if (!file) {
        puts("WRITE_BLOCKED");
        return 0;
    }
    fputs("x", file);
    fclose(file);
    puts("WRITE_ALLOWED");
    return 42;
}
""",
            "cpp": """
#include <fstream>
#include <iostream>

int main() {
    std::ofstream file("/outside-write.txt");
    if (!file) {
        std::cout << "WRITE_BLOCKED\\n";
        return 0;
    }
    file << "x";
    std::cout << "WRITE_ALLOWED\\n";
    return 42;
}
""",
            "java": """
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Files.writeString(Path.of("/outside-write.txt"), "x");
        } catch (Exception expected) {
            System.out.println("WRITE_BLOCKED");
            return;
        }
        System.out.println("WRITE_ALLOWED");
        System.exit(42);
    }
}
""",
        }

        for language, prepare, command in self.malicious_programs():
            with self.subTest(language=language):
                prepare(sources[language])
                result = self.assert_program_result(language, sources[language], command, "WRITE_BLOCKED")
                self.assertNotIn("WRITE_ALLOWED", result.stderr)

    def test_memory_limit_stops_excessive_allocation_for_all_languages(self):
        sources = {
            "python": """
chunks = []
while True:
    chunks.append(bytearray(8 * 1024 * 1024))
""",
            "c": """
#include <stdlib.h>
#include <string.h>

int main(void) {
    while (1) {
        void *chunk = malloc(8 * 1024 * 1024);
        if (!chunk) {
            return 0;
        }
        memset(chunk, 1, 8 * 1024 * 1024);
    }
}
""",
            "cpp": """
#include <cstring>
#include <new>
#include <vector>

int main() {
    std::vector<char *> chunks;
    while (true) {
        char *chunk = new char[8 * 1024 * 1024];
        std::memset(chunk, 1, 8 * 1024 * 1024);
        chunks.push_back(chunk);
    }
}
""",
            "java": """
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<byte[]> chunks = new ArrayList<>();
        while (true) {
            chunks.add(new byte[8 * 1024 * 1024]);
        }
    }
}
""",
        }

        for language, prepare, command in self.malicious_programs():
            with self.subTest(language=language):
                prepare(sources[language])
                result = self.run_sandbox(language, command, timeout_sec=8, memory_limit_kb=65536)

                self.assertFalse(result.timed_out, f"{language}: {result.stderr}")
                self.assertTrue(
                    result.oom_killed or result.exit_code in (0, 1, 137),
                    f"{language}: expected memory pressure failure or allocation refusal, got {result}",
                )

    def test_disk_output_limit_stops_large_files(self):
        self.write_file(
            "main.py",
            """
from pathlib import Path

Path("/sandbox/too-large.bin").write_bytes(b"x" * (2 * 1024 * 1024))
print("DISK_LIMIT_NOT_ENFORCED")
""",
        )

        result = self.run_sandbox("python", ["python3", "/sandbox/main.py"], timeout_sec=5, memory_limit_kb=131072)
        written = self.workdir / "too-large.bin"

        self.assertNotEqual(0, result.exit_code, result.stderr)
        self.assertNotIn("DISK_LIMIT_NOT_ENFORCED", result.stderr)
        if written.exists():
            self.assertLessEqual(written.stat().st_size, 1024 * 1024)

    def test_process_limit_blocks_fork_bomb(self):
        self.write_file(
            "main.py",
            """
import os
import sys
import time

children = []
for index in range(512):
    try:
        pid = os.fork()
    except OSError:
        print("FORK_BLOCKED", index)
        sys.exit(0)

    if pid == 0:
        time.sleep(10)
        sys.exit(0)

    children.append(pid)

print("FORK_NOT_BLOCKED", len(children))
sys.exit(42)
""",
        )

        result = self.run_sandbox("python", ["python3", "/sandbox/main.py"], timeout_sec=8, memory_limit_kb=131072)

        self.assertFalse(result.timed_out, result.stderr)
        self.assertNotEqual(42, result.exit_code, result.stderr)
        self.assertNotIn("FORK_NOT_BLOCKED", result.stderr)

    def test_persistent_child_process_is_removed_with_container(self):
        self.write_file(
            "main.py",
            """
import subprocess
import sys

process = subprocess.Popen(["sleep", "30"])
print("CHILD_STARTED", process.pid)
sys.exit(0)
""",
        )

        result = self.run_sandbox("python", ["python3", "/sandbox/main.py"], timeout_sec=5, memory_limit_kb=65536)
        running_sandboxes = self.runner.client.containers.list(filters={"label": "onlinejudge.sandbox=true"})

        self.assertEqual(0, result.exit_code, result.stderr)
        self.assertIn("CHILD_STARTED", result.stderr)
        self.assertEqual([], running_sandboxes)


if __name__ == "__main__":
    unittest.main(verbosity=2)
