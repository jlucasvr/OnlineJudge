package br.lab.testesubmissao.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SubmissionCodeStorageService {

    private final Path storageRoot;

    public SubmissionCodeStorageService(
            @Value("${oj.submissions.storage-root:./storage/submissions}") String storageRoot
    ) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    public String store(String language, String sourceCode) {
        String filename = UUID.randomUUID() + "." + extensionFor(language);
        Path outputFile = storageRoot.resolve(filename);
        try {
            Files.createDirectories(storageRoot);
            Files.writeString(outputFile, sourceCode, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao armazenar código da submissão.", ex);
        }
        return "internal://submissions/" + filename;
    }

    private String extensionFor(String language) {
        return switch (language) {
            case "c" -> "c";
            case "cpp" -> "cpp";
            case "java" -> "java";
            case "python" -> "py";
            default -> "txt";
        };
    }
}
