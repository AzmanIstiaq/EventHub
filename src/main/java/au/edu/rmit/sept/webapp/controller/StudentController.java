package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/student")
public class StudentController {
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final CategoryService categoryService;

    public StudentController(EventService eventService,
                                 RegistrationService registrationService,
                                 UserService userService,
                                 CategoryService categoryService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.categoryService = categoryService;
    }
    @GetMapping("/events")
    public String listEventsLoggedIn(@AuthenticationPrincipal CustomUserDetails currentUser,
                                     Model model) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        model.addAttribute("events", eventService.getAllUpcomingEvents());
        model.addAttribute("currentUser", user);
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pastEvents", eventService.getPastEvents());
        return "public-events";  // Thymeleaf template
    }

    // 2. Register for an event
    @PostMapping("/events/register/{eventId}")
    public String registerForEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   Model model,
                                   @PathVariable Long eventId) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        registrationService.registerUserForEvent(user, event);

        model.addAttribute("activeTab", "upcoming");

        return "redirect:/student/events";
    }

    // 2. Register for an event
    @PostMapping("/events/cancel/{eventId}")
    public String cancelEventRegistration(@AuthenticationPrincipal CustomUserDetails currentUser,
                                          Model model,
                                          @PathVariable Long eventId) {
        registrationService.deleteRegistrationForEvent(currentUser.getId(), eventId);

        model.addAttribute("activeTab", "upcoming");

        return "redirect:/student/events";  // back to event list
    }

    @GetMapping("/events/detail/{eventId}")
    public String getEventDetailLoggedIn(@AuthenticationPrincipal CustomUserDetails currentUser,
                                         Model model, @PathVariable Long eventId) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        model.addAttribute("event", event);
        model.addAttribute("currentUser", user);

        return "event-detail";
    }

    @GetMapping("/events/search")
    public String searchEventsLoggedIn(@AuthenticationPrincipal CustomUserDetails currentUser,
                                       @RequestParam(required = false) String query,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam(required = false) Long categoryId,
                                       Model model) {

        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        model.addAttribute("events", eventService.getAllUpcomingEvents());
        model.addAttribute("currentUser", user);
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pastEvents", eventService.getPastEvents());

        LocalDateTime from = (startDate != null ? startDate.atStartOfDay() : LocalDate.now().atStartOfDay());
        LocalDateTime to = (endDate != null ? endDate.atTime(23, 59) : null);

        List<Event> searchResults = eventService.searchEvents(query, from, to, categoryId);
        model.addAttribute("searchResults", searchResults);

        // add categories for dropdown
        model.addAttribute("show-search", true);

        model.addAttribute("activeTab", "search");

        model.addAttribute("currentUser", user);

        return "public-events";
    }
}
