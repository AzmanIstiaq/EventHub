package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/// This controller is to be used for all event operations
/// such as listing all events and registering for events, creating and deleting events.
@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final KeywordService keywordService;

    public EventController(EventService eventService,
                           RegistrationService registrationService,
                           UserService userService,
                           CategoryService categoryService,
                           KeywordService keywordService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.keywordService = keywordService;
    }

    @GetMapping
    public String getEvents(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        String role = currentUser.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }


        return switch (role) {
            case "ROLE_ADMIN" -> listAdminEvents(user, model);
            case "ROLE_ORGANISER" -> listOrganiserEvents(user, model);
            case "ROLE_STUDENT" -> listStudentEvents(user, model);
            default -> listEvents(user, model);
        };
    }

    @GetMapping("/public")
    public String listPublicEvents(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }

        return listEvents(user, model);
    }

    @GetMapping("/detail/{eventId}")
    public String getEventDetails(@AuthenticationPrincipal CustomUserDetails currentUser,
                                  Model model, @PathVariable Long eventId) {
        String role = currentUser.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }

        // organiser dos not have a user specific detail page
        return switch (role) {
            case "ROLE_ADMIN" -> getAdminEventDetail(user, eventId, model);
            case "ROLE_STUDENT" -> getStudentEventDetail(user, model, eventId);
            default -> getPublicEventDetail(user, model, eventId);
        };
    }

    @GetMapping("/public/detail/{eventId}")
    public String getEventPublicDetails(@AuthenticationPrincipal CustomUserDetails currentUser,Model model, @PathVariable Long eventId) {
        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }
        return getPublicEventDetail(user, model, eventId);
    }

    @GetMapping("/search")
    private String searchEvents(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @RequestParam(required = false) String query,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) Long categoryId,
                                Model model) {
        User user = userService.findById(currentUser.getUser().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
                                    
        return searchEventsGlobal(user, query, startDate, endDate, categoryId, model);
    }

    @GetMapping("/public/search")
    private String searchPublicEvents(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @RequestParam(required = false) String query,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) Long categoryId,
                                Model model) {
        User user = null;
        return searchEventsGlobal(user, query, startDate, endDate, categoryId, model);
    }

    // Student only regsiter
    @PostMapping("/register/{eventId}")
    public String registerForEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   Model model,
                                   @PathVariable Long eventId) {
        String role = currentUser.getAuthorities().iterator().next().getAuthority();

        if (Objects.equals(role, "ROLE_STUDENT")) {
            try {
                User user = userService.findById(currentUser.getUser().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
                Event event = eventService.findById(eventId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

                registrationService.registerUserForEvent(user, event);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("error", ex.getMessage());
                return "public-events";
            } catch (IllegalStateException ex) {
                model.addAttribute("error", "You are already registered for this event");
                return "public-events";
            }


            model.addAttribute("activeTab", "upcoming");
            return "redirect:/events";
        }
        return "redirect:/events/" + eventId;
    }


    @PostMapping("/deleteRegistration")
    public String deleteRegistration(@RequestParam Long userId, @RequestParam Long eventId) {
        registrationService.deleteRegistrationForEvent(userId, eventId);
        return "redirect:/events";  // Refresh the page
    }


    // Create new event for given organiser
    @PostMapping("/create")
    public String createEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @Valid @ModelAttribute Event event,
                              BindingResult bindingResult,
                              @RequestParam(required = false) String keywordsText,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            User organiser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));

            List<Event> upcomingEvents = eventService.getUpcomingEventsForOrganiser(organiser);
            List<Event> pastEvents = eventService.getPastEventsForOrganiser(organiser);

            // Add categories for form dropdown
            List<Category> categories = categoryService.findAll();

            System.out.println("DEBUG: Upcoming Events: " + upcomingEvents);
            System.out.println("DEBUG: Past Events: " + pastEvents);
            System.out.println("DEBUG: Categorey1: " + categories.get(0).getCategory());
            System.out.println("DEBUG: Organiser: " + organiser.getName());

            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("pastEvents", pastEvents);
            model.addAttribute("organiser", organiser);
            model.addAttribute("categories", categories);
            return "organiser-dashboard"; // show form again with errors
        }

        String role = currentUser.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ORGANISER") || role.equals("ROLE_ADMIN")) {
            User organiser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
            event.setOrganiser(organiser);

            if (keywordsText != null && !keywordsText.isBlank()) {
                Set<Keyword> keywordSet = Arrays.stream(keywordsText.split(","))
                        .map(String::trim)
                        .map(keywordService::findOrCreateByName)
                        .collect(Collectors.toSet());
                event.setKeywords(keywordSet);
            }

            eventService.save(event);
        }
        return "redirect:/events";
    }

    /// Updates an event based on the form input received
    @PostMapping("/{eventId}/edit")
    public String updateEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @PathVariable Long eventId,
                              @ModelAttribute Event updatedEvent,
                              @RequestParam(required = false) String keywordsText) {
        String role = currentUser.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        if (Objects.equals(role, "ROLE_ORGANISER") || Objects.equals(role, "ROLE_ADMIN")) {
            User organiser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));

            Event event = eventService.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

            // check that organiser is in fact owner of event, admin can edit any event
            if (Objects.equals(role, "ROLE_ORGANISER") && !currentUser.getId().equals(event.getOrganiser().getUserId())) {
                return "redirect:/events";
            }

            // Copy form fields
            event.setTitle(updatedEvent.getTitle());
            event.setDescription(updatedEvent.getDescription());
            event.setDateTime(updatedEvent.getDateTime());
            event.setLocation(updatedEvent.getLocation());
            event.setCategory(updatedEvent.getCategory());
            event.setOrganiser(organiser);

            // Convert comma-separated keywords to Set<Keyword>
            if (keywordsText != null && !keywordsText.isBlank()) {
                Set<Keyword> keywordSet = Arrays.stream(keywordsText.split(","))
                        .map(String::trim)
                        .map(keywordService::findOrCreateByName)
                        .collect(Collectors.toSet());
                event.setKeywords(keywordSet);
            }


            eventService.save(event);
        }
        return "redirect:/events";
    }

    // Delete inappropriate events
    @PostMapping("/{eventId}/delete")
    public String deleteEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   @PathVariable int eventId,
                              RedirectAttributes redirectAttributes) {
        String role = currentUser.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        if (Objects.equals(role, "ROLE_ORGANISER") || Objects.equals(role, "ROLE_ADMIN")) {
            // check that organiser is in fact owner of event, admin can edit any event

            try {
                Event event = eventService.findById(eventId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

                if (Objects.equals(role, "ROLE_ORGANISER") && !currentUser.getId().equals(event.getOrganiser().getUserId())) {
                    return "redirect:/events";
                }

                String eventTitle = event.getTitle();
                eventService.delete(eventId);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Event '" + eventTitle + "' has been deleted successfully.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Error deleting event: " + e.getMessage());
            }
        }

        return "redirect:/events";
    }



    // public list all events return.
    private String listEvents(User currentUser, Model model) {
        List<Event> futurEvents = new ArrayList<>();
        model.addAttribute("events", eventService.getAllUpcomingEvents());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("futureEvents", futurEvents );
        return "public-events";  // Thymeleaf template
    }

    // student list events endpoint
    private String listStudentEvents(User user,
                                     Model model) {
        List<Event> events = eventService.getEventsUserRegisteredTo(user.getUserId());

        LocalDateTime now = LocalDateTime.now();
        List<Event> pastEvents = events.stream()
                .filter(e -> e.getDateTime().isBefore(now))
                .toList();
        List<Event> futureEvents = events.stream()
                .filter(e -> e.getDateTime().isAfter(now))
                .toList();

        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("futureEvents", futureEvents);
        model.addAttribute("events", eventService.getAllUpcomingEvents());
        model.addAttribute("currentUser", user);
        model.addAttribute("activeTab", "upcoming");
        model.addAttribute("categories", categoryService.findAll());
        return "public-events";  // Thymeleaf template
    }

    // admin list all events endpoint
    private String listAdminEvents(User user, Model model) {
        List<Event> allEvents = eventService.getAllEvents();
        model.addAttribute("events", allEvents);

        // Calculate statistics
        LocalDateTime now = LocalDateTime.now();
        int upcomingEventsCount = (int) allEvents.stream()
                .filter(event -> event.getDateTime().isAfter(now))
                .count();
        int pastEventsCount = (int) allEvents.stream()
                .filter(event -> event.getDateTime().isBefore(now))
                .count();
        model.addAttribute("currentUser", user);
        model.addAttribute("upcomingEventsCount", upcomingEventsCount);
        model.addAttribute("pastEventsCount", pastEventsCount);

        return "admin-events";
    }

    // organiser list all events endpoint
    private String listOrganiserEvents(User organiser,
                                       Model model) {
        List<Event> upcomingEvents = eventService.getUpcomingEventsForOrganiser(organiser);
        List<Event> pastEvents = eventService.getPastEventsForOrganiser(organiser);

        // Add categories for form dropdown
        List<Category> categories = categoryService.findAll();

        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("organiser", organiser);
        model.addAttribute("currentUser", organiser);
        model.addAttribute("categories", categories);

        return "organiser-dashboard";
    }

    private String getAdminEventDetail(User user, @PathVariable Long eventId, Model model) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        // Get registrations for this event
        List<Registration> registrations = registrationService.getRegistrationsForEvent(event);
        
        model.addAttribute("currentUser", user);
        model.addAttribute("event", event);
        model.addAttribute("registrations", registrations);
        model.addAttribute("registrationCount", registrations.size());

        return "admin-event-detail";
    }

    private String getStudentEventDetail(User user, Model model, @PathVariable Long eventId) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        model.addAttribute("registered", event.checkUserRegistered(user.getRegistrations()));
        model.addAttribute("event", event);
        model.addAttribute("currentUser", user);

        return "event-detail";
    }

    private String getPublicEventDetail(User user, Model model, @PathVariable Long eventId) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        
        model.addAttribute("event", event);
        model.addAttribute("currentUser", user);

        return "event-detail";
    }


    private String searchEventsGlobal(User user,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long categoryId,
        Model model) {
            List<Event> futureEvents = new ArrayList<>();
            List<Event> pastEvents = new ArrayList<>();

            if (user != null) {
                List<Event> events = eventService.getEventsUserRegisteredTo(user.getUserId());

                LocalDateTime now = LocalDateTime.now();
                pastEvents = events.stream()
                        .filter(e -> e.getDateTime().isBefore(now))
                        .toList();
                futureEvents = events.stream()
                        .filter(e -> e.getDateTime().isAfter(now))
                        .toList();
            }
            

            model.addAttribute("pastEvents", pastEvents);
            model.addAttribute("futureEvents", futureEvents);
            model.addAttribute("events", eventService.getAllUpcomingEvents());
            model.addAttribute("currentUser", user);
            model.addAttribute("activeTab", "upcoming");
            model.addAttribute("categories", categoryService.findAll());

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
