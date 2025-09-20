package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/// This controller is to be used for public facing event operations (performed by student users)
/// such as listing all events and registering for events.
@Controller
@RequestMapping("/events/student")
public class PublicEventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final CategoryService categoryService;

    public PublicEventController(EventService eventService,
                                 RegistrationService registrationService,
                                 UserService userService,
                                 CategoryService categoryService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // 1. List all upcoming events
    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.getAllUpcomingEvents();
        model.addAttribute("events", events);
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        return "public-events";  // Thymeleaf template
    }

    @GetMapping("/{userId}")
    public String listEventsLoggedIn(Model model, @PathVariable Long userId) {
        List<Event> events = eventService.getAllUpcomingEvents();
        model.addAttribute("events", events);
        Optional<User> currentUser = userService.findById(userId);
        currentUser.ifPresent(user -> model.addAttribute("currentUser", user));
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pastEvents", eventService.getPastEvents());
        return "public-events";  // Thymeleaf template
    }

    // 2. Register for an event
    @PostMapping("/register/{eventId}")
    public String registerForEvent(Model model,
                                   @PathVariable Long eventId,
                                   @RequestParam Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        registrationService.registerUserForEvent(user, event);

        model.addAttribute("activeTab", "upcoming");

        return "redirect:/events/student/" + userId;  // back to event list
    }

    // 2. Register for an event
    @PostMapping("/cancel/{eventId}")
    public String cancelEventRegistration(Model model,
                                          @PathVariable Long eventId,
                                          @RequestParam Long userId) {
        registrationService.deleteRegistrationForEvent(userId, eventId);

        model.addAttribute("activeTab", "upcoming");

        return "redirect:/events/student/" + userId;  // back to event list
    }

    @GetMapping("/detail/{eventId}")
    public String getEventDetailLoggedIn(Model model, @PathVariable Long eventId,
                                         @RequestParam Long userId) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        User currentUser = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentUser);

        return "event-detail";
    }

    @GetMapping("/public/detail/{eventId}")
    public String getEventDetail(Model model, @PathVariable Long eventId) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        User currentUser = null;

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentUser);

        return "event-detail";
    }

    @GetMapping("/search")
    public String searchEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            Model model,
            @RequestParam(required = false) Long userId) {

        LocalDateTime from = (startDate != null ? startDate.atStartOfDay() : LocalDate.now().atStartOfDay());
        LocalDateTime to = (endDate != null ? endDate.atTime(23, 59) : null);

        List<Event> searchResults = eventService.searchEvents(query, from, to, categoryId);
        model.addAttribute("searchResults", searchResults);

        // add categories for dropdown
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("show-search", true);

        model.addAttribute("activeTab", "search");

        List<Event> events = eventService.getAllUpcomingEvents();
        model.addAttribute("events", events);
        if (userId != null) {
            Optional<User> currentUser = userService.findById(userId);
            currentUser.ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "public-events";
    }

}
