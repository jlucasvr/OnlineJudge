package br.lab.testesubmissao.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontedRedirectContrller {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontURL;

    @GetMapping("/")
    public String redirectToFront(){
        return "redirect:" + frontURL;
    }
}
