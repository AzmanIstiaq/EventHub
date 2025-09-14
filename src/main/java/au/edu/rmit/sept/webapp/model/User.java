package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users") // rename table from "organiser" to "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    // Enum for type of user
    @Enumerated(EnumType.STRING)
    private UserType type;

    @OneToMany(mappedBy = "user")
    private Set<Registration> registrations;

    @OneToMany(mappedBy = "organiser")
    @JsonManagedReference
    private Set<Event> organisedEvents;

    // Constructors
    public User() {}

    public User(String name, String email, String password, UserType type) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public Set<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(Set<Registration> registrations) { this.registrations = registrations; }

    public Set<Event> getOrganisedEvents() { return organisedEvents; }
    public void setOrganisedEvents(Set<Event> organisedEvents) { this.organisedEvents = organisedEvents; }

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
