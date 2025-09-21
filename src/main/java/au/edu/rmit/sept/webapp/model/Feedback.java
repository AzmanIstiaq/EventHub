package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "event", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int rating;  // 1-5

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Feedback() {}

    public Feedback(Event event, User user, int rating, String comment) {
        this.event = event;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }
    public Long getId() {
        return id;
    }
    public Event getEvent() {
        return event;
    }
    public User getUser() {
        return user;
    }
    public int getRating() {
        return rating;
    }
    public String getComment() {
        return comment;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}
