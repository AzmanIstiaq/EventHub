package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final EventRepository eventRepo;
    private final EventService eventService;
    private final UserService userService;

    public HomeController(EventRepository eventRepo, EventService eventService, UserService userService) {
        this.eventRepo = eventRepo;
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }

        List<Event> event = eventRepo.findByDateTimeAfterOrderByDateTimeAsc(java.time.LocalDateTime.now());

        model.addAttribute("currentUser", user);
        model.addAttribute("events", event); // Pass events to the view
        return "index"; // Loads templates/index.html
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        // Get summary statistics
        List<Event> allEvents = eventService.getAllEvents();
        List<User> allUsers = userService.getAllUsers();
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        model.addAttribute("currentUser", user);
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("recentEvents", allEvents.stream().limit(5).toList());

        return "admin-dashboard";
    }
}
