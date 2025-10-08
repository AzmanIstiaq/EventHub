package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Column(name = "ban_type", nullable = false)
    private BanType banType;

    @Column(name = "ban_end_date", nullable = true)
    private LocalDateTime banEndDate;

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "user_id", nullable = false)
    private User admin;

    // -- Constructor --
    public Ban() {}

    // Ban with all details
    public Ban(User user, User admin, BanType banType, String banReason, LocalDateTime banEndDate) {
        this.user = user;
        this.banReason = banReason;
        this.banType = banType;
        this.banEndDate = banEndDate;
        this.admin = admin;
    }

    // Permananent ban (i.e. no end date)
    public Ban(User user, User admin, BanType banType, String banReason) {
        this.user = user;
        this.banReason = banReason;
        this.banType = banType;
        this.banEndDate = null;
        this.admin = admin;
    }

    // -- Getters and Setters --
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public Long getBanId() { return banId; }

    public void setBanType(BanType banType) { this.banType = banType; }
    public BanType getBanType() { return banType; }

    public void setBanEndDate(LocalDateTime banEndDate) { this.banEndDate = banEndDate; }
    public LocalDateTime getBanEndDate() { return banEndDate; }

    public void setAdmin(User admin) { this.admin = admin; }
    public User getAdmin() { return admin; }
}
