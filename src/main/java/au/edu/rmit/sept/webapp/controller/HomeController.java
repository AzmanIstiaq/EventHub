package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EventRepository eventRepo;

    public HomeController(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("events", eventRepo.findAll()); // Pass events to the view
        return "index"; // Loads templates/index.html
    }
}
