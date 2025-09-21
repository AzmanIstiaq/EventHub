package au.edu.rmit.sept.webapp.controller;


import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/student/events")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    private final EventService eventService;
    private final UserService userService;

    public FeedbackController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }


    @PostMapping("/{eventId}/feedback")
    public String submitFeedback(@PathVariable Long eventId,
                                 @RequestParam int rating,
                                 @RequestParam String comment,
                                 @RequestParam Long userId) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        User currentUser = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));
        feedbackService.submitFeedback(currentUser, event, rating, comment);
        return "redirect:/student/events";
    }
}
