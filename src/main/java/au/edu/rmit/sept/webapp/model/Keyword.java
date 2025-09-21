package au.edu.rmit.sept.webapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;


@Entity()
@Table(
        name = "keywords",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "keyword"})
        }
)
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", nullable = false)
    private int keywordId;

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    @JsonBackReference
    private Event event;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    // -- Constructor -- //
    public Keyword() {}
    public Keyword(Event event, String keyword) {
        this.event = event;
        this.keyword = keyword;
    }

    // -- Getters and Setters -- //
    public int getKeywordId() { return keywordId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
