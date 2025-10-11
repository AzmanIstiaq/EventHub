package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// forward to admin/events/..
@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final KeywordService keywordService;

    public AdminEventController(EventService eventService,
                                CategoryService categoryService,
                                KeywordService keywordService) {
        this.eventService = eventService;
        this.categoryService = categoryService;
        this.keywordService = keywordService;
    }

    // --- Show Admin Edit Form ---
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

        model.addAttribute("event", event);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentUser", currentUser.getUser());
        return "admin-event-edit";
    }

    // --- Process Update Form ---
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

        return "redirect:/events";
    }
}
