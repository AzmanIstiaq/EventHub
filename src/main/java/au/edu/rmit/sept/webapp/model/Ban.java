package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;

@Entity()
@Table(name = "bans")
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id")
    private Long banId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "ban_reason", nullable = false)
    private String banReason;

    // -- Constructor --
    public Ban() {}

    // Ban with reason
    public Ban(User user, String banReason) {
        this.user = user;
        this.banReason = banReason;
    }

    // Ban without reason
    public Ban(User user) {
        this.user = user;
        this.banReason = " --- No reason provided --- ";
    }

    // -- Getters and Setters --
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public Long getBanId() { return banId; }
}
