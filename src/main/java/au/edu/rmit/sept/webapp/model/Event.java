package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int eventId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "rsvp_slots", nullable = false)
    private int rsvpSlots;

    @Column(name = "location", nullable = false)
    private String location;

    // Organiser (many-to-one)
    @ManyToOne
    @JoinColumn(name = "organiser_id", nullable = false)
    @JsonBackReference
    private User organiser;

    // Tags (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Tag> tags;

    // Category (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Category> categories;

    // Event gallery (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<EventGallery> photos;

    // RSVPS (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Registration> registrations;

    // Feedback (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Feedback> feedback;

    // Keywords (one-to-many)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Keyword> keywords;

    // --- Constructors ---
    public Event() {}

    public Event(String title, String description, LocalDateTime eventDate,
                 LocalDateTime creationDate, int rsvpSlots, String location,
                 User organiser) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.creationDate = creationDate;
        this.rsvpSlots = rsvpSlots;
        this.location = location;
        this.organiser = organiser;
        this.categories = new HashSet<>();
        this.tags = new HashSet<>();
        this.photos = new HashSet<>();
        this.registrations = new HashSet<>();
        this.feedback = new HashSet<>();
        this.keywords = new HashSet<>();
    }

    // --- Getters and Setters ---
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = this.eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creation_date) { this.creationDate = creation_date; }

    public int getRsvpSlots() { return rsvpSlots; }
    public void setRsvpSlots(int rsvpSlots) { this.rsvpSlots = rsvpSlots; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getOrganiser() { return organiser; }
    public void setOrganiser(User organiser) { this.organiser = organiser; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }

    public Set<EventGallery> getPhotos() { return photos; }
    public void setPhotos(Set<EventGallery> photos) { this.photos = photos; }

    public Set<Registration> getRsvps() { return registrations; }
    public void setRsvps(Set<Registration> registrations) { this.registrations = registrations; }

    public Set<Feedback> getFeedback() { return feedback; }
    public void setFeedback(Set<Feedback> feedback) { this.feedback = feedback; }

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
        keyword.setEvent(this);
    }

    public void removeKeyword(Keyword keyword) {
        this.keywords.remove(keyword);
        keyword.setEvent(null);
    }

    public void addCategory(String category) {
        Category newCategory = new Category(this, category);
        this.categories.add(newCategory);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
    }

    @Override
    public String toString() {
        return "Event{" +
                "event_id=" + eventId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", eventDate=" + eventDate +
                ", creation_date=" + creationDate +
                ", rsvp_slots=" + rsvpSlots +
                ", location='" + location +
                '}';
    }
}
