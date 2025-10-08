package au.edu.rmit.sept.webapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminUserId;

    @Column(nullable = false)
    private String action; // e.g. EVENT_EDIT, EVENT_HIDE, EVENT_UNHIDE, EVENT_DELETE, FEEDBACK_DELETE

    @Column(nullable = false)
    private Long targetId; // eventId or feedbackId

    @Column(nullable = false)
    private String targetType; // "EVENT" | "FEEDBACK"

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(length = 2000)
    private String details; // optional

    public Long getId() { return id; }
    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
