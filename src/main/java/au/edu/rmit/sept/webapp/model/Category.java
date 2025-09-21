package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;


@Entity()
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "category"})
        }
)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    private Event event;

    @Column(name = "category", nullable = false)
    private String category;

    // -- Constructor -- //
    public Category() {}

    public Category(Event event, String category) {

        this.event = event;
        this.category = category;
    }

    // -- Getters and Setters -- //
    public int getCategoryId() { return categoryId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
