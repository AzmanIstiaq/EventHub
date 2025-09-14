package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users") // rename table from "organiser" to "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    // Enum for type of user
    @Enumerated(EnumType.STRING)
    private UserType type;

    // Optional: If this user is an organiser and has events
    @OneToMany(mappedBy = "organiser", cascade = CascadeType.ALL)
    private List<Event> events;

    // Constructors
    public User() {}

    public User(String name, String email, UserType type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    // Helper methods
    public boolean isOrganiser() {
        return this.type == UserType.ORGANISER;
    }

    public boolean isAdmin() {
        return this.type == UserType.ADMIN;
    }

    public boolean isStudent() {
        return this.type == UserType.STUDENT;
    }

}
