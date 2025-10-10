package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.AuditLog;
import au.edu.rmit.sept.webapp.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AuditLog record(Long adminUserId, String action, String targetType, Long targetId, String details) {
        AuditLog log = new AuditLog();
        log.setAdminUserId(adminUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        return repo.save(log);
    }
}
