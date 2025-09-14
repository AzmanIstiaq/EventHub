package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;
    private String location;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference
    private Set<Registration> registrations;

    @ManyToOne
    @JoinColumn(name = "organiser_id", nullable = false)
    @JsonBackReference
    private User organiser;

    public Event() {}

    public Event(String title, String description, LocalDateTime dateTime,
                 String location, User organiser) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.organiser = organiser;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Set<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(Set<Registration> registrations) { this.registrations = registrations; }

    public User getOrganiser() { return organiser; }
    public void setOrganiser(User organiser) { this.organiser = organiser; }


    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                ", location='" + location + '\'' +
                '}';
    }
}
