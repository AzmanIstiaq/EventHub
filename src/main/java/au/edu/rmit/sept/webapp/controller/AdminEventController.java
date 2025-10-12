package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.AuditLogService;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import au.edu.rmit.sept.webapp.model.Registration;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final KeywordService keywordService;
    private final AuditLogService auditLogService;
    private final RegistrationService registrationService;
    private final UserService userService;

    public AdminEventController(EventService eventService,
                                CategoryService categoryService,
                                KeywordService keywordService,
                                AuditLogService auditLogService,
                                RegistrationService registrationService,
                                UserService userService) {
        this.eventService = eventService;
        this.categoryService = categoryService;
        this.keywordService = keywordService;
        this.auditLogService = auditLogService;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    // Admin detailed view
    @GetMapping("/{id}")
    public String getAdminEventDetailPage(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetails currentUser,
                                          Model model) {

        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        User user = null;
        if (currentUser != null) {
            user = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("event", event);
        model.addAttribute("registrations", registrationService.getRegistrationsForEvent(event));
        model.addAttribute("registrationCount", registrationService.getRegistrationsForEvent(event).size());
        return "admin-event-detail";
    }

    // Show admin edit form
    @GetMapping("/{id}/edit")
    public String showAdminEditForm(@PathVariable Long id,
                                    Model model,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        String role = currentUser.getAuthorities().iterator().next().getAuthority();
        if (!Objects.equals(role, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        model.addAttribute("event", event);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentUser", user);
        return "admin-event-edit";
    }

    // Process update form
    @PostMapping("/{id}/edit")
    public String updateAdminEvent(@PathVariable Long id,
                                   @ModelAttribute Event updatedEvent,
                                   @RequestParam(required = false) String keywordsText,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {

        String role = currentUser.getAuthorities().iterator().next().getAuthority();
        if (!Objects.equals(role, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setDateTime(updatedEvent.getDateTime());
        event.setLocation(updatedEvent.getLocation());
        event.setCategory(updatedEvent.getCategory());

        if (keywordsText != null && !keywordsText.isBlank()) {
            Set<Keyword> keywordSet = Arrays.stream(keywordsText.split(","))
                    .map(String::trim)
                    .map(keywordService::findOrCreateByName)
                    .collect(Collectors.toSet());
            event.setKeywords(keywordSet);
        }

        eventService.save(event);
        return "redirect:/events/detail/" + id;
    }

    @PostMapping("/{id}/hide")
    public String hideEvent(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails admin) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        event.setHidden(true);
        eventService.save(event);
        if (admin != null) {
            auditLogService.record(admin.getId(), AdminAction.EVENT_HIDE, AdminTargetType.EVENT, id, "Event hidden by admin");
        }
        return "redirect:/admin/events/" + id;
    }

    @PostMapping("/{id}/unhide")
    public String unhideEvent(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails admin) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        event.setHidden(false);
        eventService.save(event);
        if (admin != null) {
            auditLogService.record(admin.getId(), AdminAction.EVENT_UNHIDE, AdminTargetType.EVENT, id, "Event unhidden by admin");
        }
        return "redirect:/admin/events/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails admin) {
        eventService.deleteById(id);
        if (admin != null) {
            auditLogService.record(admin.getId(), AdminAction.EVENT_DELETE, AdminTargetType.EVENT, id, "Event deleted by admin");
        }
        return "redirect:/events";
    }

    // NEW CSV EXPORT ENDPOINT
    @GetMapping("/{eventId}/attendees.csv")
    public void exportAttendeesCsvSingleEvent(@PathVariable Long eventId,
                                              @AuthenticationPrincipal CustomUserDetails admin,
                                              HttpServletResponse response) throws IOException {

        Optional<Event> eventOpt = eventService.findById(eventId);
        if (eventOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Event event = eventOpt.get();
        List<?> attendees = registrationService.getRegistrationsForEvent(event);

        String filename = "event-#" + eventId + "-attendees.csv";
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("attendeeId,attendeeName,email,eventStatus,registeredAt");

            DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (Object obj : attendees) {
                var r = (Registration) obj;

                String futureOrPastStatus = r.getEvent().getDateTime().isAfter(java.time.LocalDateTime.now()) ? "Upcoming" : "Past";

                String registeredAt = r.getRegistrationDate() != null ? r.getRegistrationDate().format(dtf) : "";
                String line = String.format("%d,%s,%s,%s,%s",
                        r.getRegistrationId(),
                        escapeCsv(r.getUser().getName()),
                        escapeCsv(r.getUser().getEmail()),
                        escapeCsv(futureOrPastStatus),
                        escapeCsv(registeredAt)
                );
                writer.println(line);
            }
            writer.flush();
        }

    }

    // CSV Download for all events with attendees
    @GetMapping("/all/attendees.csv")
    public void downloadAllEventsCsv(@AuthenticationPrincipal CustomUserDetails admin,
                                     HttpServletResponse response) throws IOException {

        List<Event> events = eventService.getAllEvents();

        String filename = "all-events-with-attendees.csv";
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            // CSV header
            writer.println("eventId,title,description,dateTime,location,category,keywords,hidden,eventStatus,attendeeId,attendeeName,email,registeredAt");

            DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (Event event : events) {
                String keywords = event.getKeywords().stream()
                        .map(Keyword::getKeyword)
                        .collect(Collectors.joining(";"));

                // Determine event status
                String eventStatus = event.getDateTime().isAfter(LocalDateTime.now()) ? "Upcoming" : "Past";

                // Get registrations for the event
                List<Registration> registrations = registrationService.getRegistrationsForEvent(event);

                // If no attendees, still output one row with empty attendee fields
                if (registrations.isEmpty()) {
                    String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,,,,",
                            event.getEventId(),
                            escapeCsv(event.getTitle()),
                            escapeCsv(event.getDescription()),
                            escapeCsv(event.getDateTime().format(dtf)),
                            escapeCsv(event.getLocation()),
                            escapeCsv(event.getCategory() != null ? event.getCategory().getCategory() : ""),
                            escapeCsv(keywords),
                            event.isHidden() ? "Yes" : "No",
                            eventStatus
                    );
                    writer.println(line);
                } else {
                    // Output one row per attendee
                    for (Registration r : registrations) {
                        String registeredAt = r.getRegistrationDate() != null ? r.getRegistrationDate().format(dtf) : "";
                        String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s",
                                event.getEventId(),
                                escapeCsv(event.getTitle()),
                                escapeCsv(event.getDescription()),
                                escapeCsv(event.getDateTime().format(dtf)),
                                escapeCsv(event.getLocation()),
                                escapeCsv(event.getCategory() != null ? event.getCategory().getCategory() : ""),
                                escapeCsv(keywords),
                                event.isHidden() ? "Yes" : "No",
                                eventStatus,
                                r.getUser().getUserId(),
                                escapeCsv(r.getUser().getName()),
                                escapeCsv(r.getUser().getEmail()),
                                escapeCsv(registeredAt)
                        );
                        writer.println(line);
                    }
                }
            }
            writer.flush();
        }
    }


    // helper for CSV escaping
    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
