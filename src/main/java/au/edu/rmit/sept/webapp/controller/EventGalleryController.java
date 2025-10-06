package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventGallery;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.service.EventGalleryService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/events/{eventId}/gallery")
public class EventGalleryController {
    private final EventRepository eventRepository;
    private final EventGalleryService galleryService;
    private final UserService userService;

    public EventGalleryController(EventRepository eventRepository, EventGalleryService galleryService, UserService userService) {
        this.eventRepository = eventRepository;
        this.galleryService = galleryService;
        this.userService = userService;
    }

    @GetMapping
    public String viewGallery(@PathVariable Long eventId, Model model) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        List<EventGallery> photos = galleryService.getPhotosByEvent(event);
        model.addAttribute("event", event);
        model.addAttribute("photos", photos);
        return "event-gallery";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable Long eventId, @RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        /// /////////////////////////////////////////////////////////////////
        /// /////////////////////////////////////////////////////////////////
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        Long currentUserId = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getUserId();

        if (!event.getOrganiser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot upload photos to events you don’t own.");
        }
        /// /////////////////////////////////////////////////////////////////
        /// /////////////////////////////////////////////////////////////////
        galleryService.uploadPhoto(event, file);
        return "redirect:/events/detail/" + eventId;
    }

    @GetMapping("/upload")
    public String uploadGetRedirect(@PathVariable Long eventId) {
        return "redirect:/events/detail/" + eventId;
    }
}
