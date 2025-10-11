package au.edu.rmit.sept.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Authentication auth,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (auth != null && auth.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("info", "You are already logged in.");
            return "redirect:/events";
        }
        return "login";
    }
}
