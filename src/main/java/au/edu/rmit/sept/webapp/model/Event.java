package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    @Column(nullable = false)
    private String location;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Registration> registrations;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Feedback> feedbacks;

    @ManyToOne
    @JoinColumn(name = "organiser_id", nullable = false)
    @JsonBackReference
    private User organiser;

    // Category (single choice)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Keywords / tags (many-to-many)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "event_keyword",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private Set<Keyword> keywords = new HashSet<>();

    // --- Constructors ---
    public Event() {}

    public Event(String title, String description, LocalDateTime dateTime,
                 String location, User organiser, Category category) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.organiser = organiser;
        this.category = category;
    }

    // --- Getters and setters ---
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

    public Set<Feedback> getFeedbacks() { return feedbacks; }

    public Boolean checkUserRegistered(User user) {
        for (Registration registration : registrations) {
            if (registration.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    public User getOrganiser() { return organiser; }
    public void setOrganiser(User organiser) { this.organiser = organiser; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Set<Keyword> getKeywords() { return keywords; }
    public void setKeywords(Set<Keyword> keywords) { this.keywords = keywords; }

    public String getKeywordsAsString() {
        if (keywords == null || keywords.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Keyword word : keywords) {
            sb.append(word.getName()).append(", ");
        }

        // Remove the last comma and space
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }


    // Utility method to add a keyword
    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        keyword.getEvents().add(this);
    }

    public void removeKeyword(Keyword keyword) {
        this.keywords.remove(keyword);
        keyword.getEvents().remove(this);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                ", location='" + location + '\'' +
                ", category=" + (category != null ? category.getName() : null) +
                '}';
    }
}
