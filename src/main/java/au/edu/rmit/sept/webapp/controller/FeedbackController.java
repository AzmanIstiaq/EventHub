package au.edu.rmit.sept.webapp.controller;


import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/events")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    private final EventService eventService;
    private final UserService userService;

    public FeedbackController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }


    @PostMapping("/feedback/{eventId}")
    public String submitFeedback(@PathVariable Long eventId,
                                 @RequestParam int rating,
                                 @RequestParam String comment,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        feedbackService.submitFeedback(user, event, rating, comment);

        return "redirect:/events";
    }
}
