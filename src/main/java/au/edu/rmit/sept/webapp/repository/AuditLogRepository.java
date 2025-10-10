package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }
