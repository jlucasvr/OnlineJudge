package br.lab.testesubmissao.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Armazena o código-fonte da submissão no sistema de arquivos seguindo a estrutura:
 *
 *   data/submissions/{year}/{month}/{uuid}.c
 *
 * O caminho salvo no banco (code_path) é o path absoluto dentro do volume compartilhado,
 * permitindo que o Worker leia o arquivo diretamente do mesmo volume montado.
 */
@Service
public class SubmissionCodeStorageService {

    private final Path dataRoot;

    public SubmissionCodeStorageService(
            @Value("${oj.data.root:/data}") String dataRoot
    ) {
        this.dataRoot = Paths.get(dataRoot).toAbsolutePath().normalize();
    }

    /**
     * Salva o código-fonte e retorna o path absoluto do arquivo gerado.
     * Estrutura: {dataRoot}/submissions/{year}/{month}/{uuid}.{ext}
     */
    public String store(String language, String sourceCode) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        Path dir = dataRoot.resolve("submissions").resolve(year).resolve(month);
        String filename = UUID.randomUUID() + "." + extensionFor(language);
        Path outputFile = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
            Files.writeString(outputFile, sourceCode, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao armazenar código da submissão: " + ex.getMessage(), ex);
        }

        // Retorna path absoluto — o Worker acessa o mesmo arquivo via volume compartilhado
        return outputFile.toString();
    }

    private String extensionFor(String language) {
        return switch (language.toLowerCase()) {
            case "c"      -> "c";
            case "cpp"    -> "cpp";
            case "java"   -> "java";
            case "python" -> "py";
            default       -> "txt";
        };
    }
}
