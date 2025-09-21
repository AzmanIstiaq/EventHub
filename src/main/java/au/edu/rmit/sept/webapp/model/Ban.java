package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;

@Entity()
@Table(name = "bans")
public class Ban {
    @Id
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "ban_reason", nullable = false)
    private String banReason;

    // -- Constructor --
    public Ban() {}

    public Ban(User user, String banReason) {
        this.user = user;
        this.banReason = banReason;
    }

    // -- Getters and Setters --
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }
}
