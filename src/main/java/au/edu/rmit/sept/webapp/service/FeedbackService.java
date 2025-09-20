package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback submitFeedback(User user, Event event, int rating, String comment) {
        if (feedbackRepository.findByEventAndUser(event, user).isPresent()) {
            throw new IllegalStateException("User has already submitted feedback for this event");
        }

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEvent(event);
        feedback.setRating(rating);
        feedback.setComment(comment);

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackForEvent(Event event) {
        return feedbackRepository.findAllByEvent(event);
    }
}
