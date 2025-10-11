package au.edu.rmit.sept.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorControllerCustom {

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }
}

