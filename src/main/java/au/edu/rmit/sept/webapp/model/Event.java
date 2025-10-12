package au.edu.rmit.sept.webapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;
    @NotBlank(message = "Title is required")
    private String title;

    @Column(length = 1000)
    @NotBlank(message = "Description is required")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Date/Time is required")
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @NotBlank(message = "Location is required")
    private String location;

    @Column(nullable = false)
    private boolean hidden = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Registration> registrations = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Feedback> feedbacks = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "organiser", nullable = false)
    @JsonBackReference
    private User organiser;

    // Category (single choice)
    @ManyToOne(fetch = FetchType.EAGER)
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
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public boolean isInPast() {
        return this.dateTime.isBefore(LocalDateTime.now());
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Set<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(Set<Registration> registrations) { this.registrations = registrations; }

    public Set<Feedback> getFeedbacks() { return feedbacks; }

    public Boolean checkUserRegistered(Set<Registration> userRegistrations) {
        for (Registration registration : userRegistrations) {
            if (registration.getEvent().eventId.equals(eventId)) return true;
        }
        return false;
    }

    public double getStarRating() {
        if (feedbacks.isEmpty()) {
            return 0;
        }
        double score = 0;
        for (Feedback feedback : feedbacks) {
            score += feedback.getRating();
        }
        return score / feedbacks.size();
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
            sb.append(word.getKeyword()).append(", ");
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
                "id=" + eventId +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                ", location='" + location + '\'' +
                ", category=" + (category != null ? category.getCategory() : null) +
                '}';
    }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

}