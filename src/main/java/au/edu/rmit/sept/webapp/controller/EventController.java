package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class EventController {
    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping("/organiser/events")
    public String organiserEvents(Model model) {
        // Hardcoding organiserId=1 for now
        List<Event> events = service.getUpcomingEventsForOrganiser(1L);
        model.addAttribute("events", events);
        return "organiser-events";
    }
}