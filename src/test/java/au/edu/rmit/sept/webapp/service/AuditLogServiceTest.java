package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.AdminAction;
import au.edu.rmit.sept.webapp.model.AdminTargetType;
import au.edu.rmit.sept.webapp.model.AuditLog;
import au.edu.rmit.sept.webapp.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository repo;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    @DisplayName("record() creates and saves audit log with all fields")
    void recordCreatesAuditLog() {
        Long adminUserId = 1L;
        AdminAction action = AdminAction.EVENT_DELETE;
        AdminTargetType targetType = AdminTargetType.EVENT;
        Long targetId = 100L;
        String details = "Deleted inappropriate event";

        auditLogService.record(adminUserId, action, targetType, targetId, details);

        verify(repo).save(any(AuditLog.class));
    }
}
