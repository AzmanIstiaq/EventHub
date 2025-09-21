package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity()
@Table(
        name = "rsvps",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "student_id"})
        }
)
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id", nullable = false)
    private int registrationId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonBackReference
    private User student;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    // -- Constructors -- //
    public Registration() {}
    public Registration(User student, Event event, LocalDateTime date) {
        this.event = event;
        this.student = student;
        this.date = date;
    }

    // -- Getters and Setters -- //
    public int getRegistrationId() { return registrationId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
