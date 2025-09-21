package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "event", nullable = false)
    private Event event;

    private LocalDateTime registrationDate = LocalDateTime.now();

    public Registration() {}

    public Registration(User user, Event event) {
        this.user = user;
        this.event = event;
    }
    // Getters and setters
    public Long getRegistrationIdId() { return registrationId; }
    public void setRegistrationIdId(Long registrationId) { this.registrationId = this.registrationId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

}