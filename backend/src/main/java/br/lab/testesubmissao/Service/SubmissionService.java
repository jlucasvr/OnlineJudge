package br.lab.testesubmissao.Service;

import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class SubmissionService {
    @Autowired
    private SubmissionRepository submissionRepository;

    public void create(Submission submission){
        submission.setSaida(null);
        submission.setStatus("Pendente");
        this.submissionRepository.save(submission);
    }

    public Submission buscarPorId(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));
    }

    public Map<String, String> Processar(Map<String, String> dados){
        Submission submission = new Submission();
        submission.setCodigo(dados.get("code"));
        submission.setLinguagem(dados.get("lang"));
        submission.setStatus("pendente");
        submissionRepository.save(submission);

        Submission salva = submissionRepository.findById(submission.getId())
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));

        String BASE = System.getProperty("user.dir");
        String cppPath = BASE + "/arquivo.c";
        String exePath = BASE + "/programa";

        try {
            Files.writeString(Paths.get(cppPath), salva.getCodigo());
            ProcessBuilder compile = new ProcessBuilder("gcc", cppPath, "-o", exePath);
            compile.redirectErrorStream(true);
            Process compileProcess = compile.start();
            String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
            int compileCode = compileProcess.waitFor();

            if(compileCode != 0){
                salva.setSaida(compileOutput);
                salva.setStatus("error");
                submissionRepository.save(salva);
                return Map.of("status", "error", "resultado", compileOutput);
            }

            ProcessBuilder run = new ProcessBuilder(exePath);
            Process runProcess = run.start();
            String result = new String(runProcess.getInputStream().readAllBytes());
            runProcess.waitFor();



            salva.setSaida(result);
            if(salva.getSaida().equals("Hello world\n")){

            }

            salva.setStatus("concluido");
            submissionRepository.save(salva);

            return Map.of("status", "ok", "resultado", result);

        } catch (Exception e) {
            salva.setStatus("erro");
            salva.setSaida(e.getMessage());
            submissionRepository.save(salva);
            return Map.of("status", "erro", "resultado", e.getMessage());
        }
    }
}
