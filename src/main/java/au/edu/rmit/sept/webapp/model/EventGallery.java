package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;

@Entity()
@Table(
        name = "event_gallery",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "photo"})
        }
)
public class EventGallery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id", nullable = false)
    private long photoId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "photo", nullable = false)
    private String photo;

    // -- Constructor -- //
    public EventGallery() {}
    public EventGallery(Event event, String photo) {
        this.event = event;
        this.photo = photo;
    }

    // -- Getters and Setters -- //
    public long getPhotoId() { return photoId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

}
