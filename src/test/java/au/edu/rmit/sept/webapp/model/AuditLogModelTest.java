package au.edu.rmit.sept.webapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogModelTest {

    @Test
    @DisplayName("AuditLog getters and setters work correctly")
    void auditLogGettersSetters() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.setAdminUserId(100L);
        auditLog.setAction(AdminAction.EVENT_DELETE);
        auditLog.setTargetType(AdminTargetType.EVENT);
        auditLog.setTargetId(200L);
        auditLog.setDetails("Deleted inappropriate event");
        LocalDateTime timestamp = LocalDateTime.now();
        auditLog.setTimestamp(timestamp);

        assertThat(auditLog.getAdminUserId()).isEqualTo(100L);
        assertThat(auditLog.getAction()).isEqualTo(AdminAction.EVENT_DELETE);
        assertThat(auditLog.getTargetType()).isEqualTo(AdminTargetType.EVENT);
        assertThat(auditLog.getTargetId()).isEqualTo(200L);
        assertThat(auditLog.getDetails()).isEqualTo("Deleted inappropriate event");
        assertThat(auditLog.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("AuditLog default constructor sets timestamp")
    void auditLogDefaultConstructorSetsTimestamp() {
        AuditLog auditLog = new AuditLog();
        
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
}
