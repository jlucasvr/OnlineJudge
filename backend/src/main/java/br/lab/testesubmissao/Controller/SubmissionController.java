package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Entity.Submission;
import br.lab.testesubmissao.Service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("")
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("sucesso/{id}")
    public Submission sucesso(@PathVariable Long id){
        return submissionService.buscarPorId(id);
    }

    @PostMapping("/enviar")
    public Map<String, String> compilar(@RequestBody Map<String, String> dados){
        return submissionService.Processar(dados);
    }
}
