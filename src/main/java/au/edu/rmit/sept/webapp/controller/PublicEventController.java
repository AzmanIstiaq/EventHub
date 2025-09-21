package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/// This controller is to be used for public facing event operations (performed by student users)
/// such as listing all events and registering for events.
@Controller
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;

    public PublicEventController(EventService eventService,
                                 RegistrationService registrationService,
                                 UserService userService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    // 1. List all upcoming events
    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.getAllUpcomingEvents();
        model.addAttribute("events", events);
        return "public-events";  // Thymeleaf template
    }

    // 2. Register for an event
    @PostMapping("/{eventId}/register")
    public String registerForEvent(@PathVariable int eventId,
                                   @RequestParam int userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        registrationService.registerUserForEvent(user, event);

        return "redirect:/events";  // back to event list
    }
}
