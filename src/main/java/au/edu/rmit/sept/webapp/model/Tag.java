package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;


@Entity()
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "tag"})
        }
)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false)
    private long tagId;

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    @JsonBackReference
    private Event event;

    @Column(name = "tag", nullable = false)
    private String tag;

    // -- Constructors -- //
    public Tag() {}
    public Tag(Event event, String tag) {
        this.event = event;
        this.tag = tag;
    }

    // -- Getters and Setters -- //
    public long getTagId() { return tagId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}
