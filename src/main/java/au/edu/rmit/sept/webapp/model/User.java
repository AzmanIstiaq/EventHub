package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false)
    private UserType role;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    // Bans (one-to-one)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Ban ban;

    // Events (one-to-many)
    @OneToMany(mappedBy = "organiser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Event> events;

    // Feedback (one-to-many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Feedback> feedback;

    // RSVP's (one-to-many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Registration> registrations;

    // -- Constructors -- //
    public User() {}
    public User(String name, String email, String password, UserType role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
        this.events = new HashSet<>();
        this.registrations = new HashSet<>();
        this.feedback = new HashSet<>();
        this.ban = null;
        this.joinDate = LocalDateTime.now();
    }

    // -- Getters and Setters -- //
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getRole() { return role; }
    public void setRole(UserType role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Registration> getRegistrations() { return registrations; }
    public void getRegistrations(Set<Registration> registrations) { this.registrations = registrations; }

    public Set<Event> getOrganisedEvents() { return events; }
    public void setOrganisedEvents(Set<Event> events) { this.events = events; }

    public Ban getBan() { return ban; }
    public void setBan(Ban ban) { this.ban = ban; }

    public Set<Feedback> getFeedback() { return feedback; }
    public void setFeedback(Set<Feedback> feedback) { this.feedback = feedback; }

    public LocalDateTime getJoinDate() { return joinDate; }

    // Helper methods
    public boolean isOrganiser() {
        return this.role == UserType.ORGANISER;
    }

    public boolean isAdmin() {
        return this.role == UserType.ADMIN;
    }

    public boolean isStudent() {
        return this.role == UserType.STUDENT;
    }
}
