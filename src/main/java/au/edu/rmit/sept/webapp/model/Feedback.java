package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity()
@Table(
        name = "feedback",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "user_id"})
        }
)
public class Feedback {
    @Id
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "feedback", length = 1000, nullable = false)
    private String feedback;

    @Column(name = "rating", nullable = false)
    private int rating;

    // -- Constructor -- //
    public Feedback() {}
    public Feedback(Event event, User user, LocalDateTime date, String feedback, int rating) {
        this.event = event;
        this.user = user;
        this.date = date;
        this.feedback = feedback;
        this.rating = rating;
    }

    // -- Getters and Setters -- //
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
