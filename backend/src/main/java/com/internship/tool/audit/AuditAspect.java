package com.internship.tool.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.AuditLog;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.AuditLogRepository;
import com.internship.tool.repository.RegulatoryChangeRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditLogRepository auditLogRepository;
    private final RegulatoryChangeRepository changeRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository, 
                       RegulatoryChangeRepository changeRepository, 
                       ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.changeRepository = changeRepository;
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.internship.tool.service.RegulatoryChangeService.createChange(..))")
    public Object auditCreate(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        
        if (result instanceof RegulatoryChange createdChange) {
            saveAuditLog("RegulatoryChange", createdChange.getId(), "CREATE", null, createdChange);
        }
        
        return result;
    }

    @Around("execution(* com.internship.tool.service.RegulatoryChangeService.updateChange(Long, ..)) && args(id, ..)")
    public Object auditUpdate(ProceedingJoinPoint pjp, Long id) throws Throwable {
        Optional<RegulatoryChange> oldStateOpt = changeRepository.findById(id);
        RegulatoryChange oldState = oldStateOpt.map(this::cloneChange).orElse(null);

        Object result = pjp.proceed();

        if (result instanceof RegulatoryChange newState) {
            saveAuditLog("RegulatoryChange", id, "UPDATE", oldState, newState);
        }

        return result;
    }

    @Around("execution(* com.internship.tool.service.RegulatoryChangeService.softDelete(Long)) && args(id)")
    public Object auditDelete(ProceedingJoinPoint pjp, Long id) throws Throwable {
        Optional<RegulatoryChange> oldStateOpt = changeRepository.findById(id);
        RegulatoryChange oldState = oldStateOpt.map(this::cloneChange).orElse(null);

        Object result = pjp.proceed();

        // Fetch the new state (which should have isDeleted = true)
        RegulatoryChange newState = changeRepository.findById(id).orElse(null);
        saveAuditLog("RegulatoryChange", id, "DELETE", oldState, newState);

        return result;
    }

    private void saveAuditLog(String entityType, Long entityId, String operation, Object oldObj, Object newObj) {
        try {
            String oldValueJson = oldObj != null ? objectMapper.writeValueAsString(oldObj) : null;
            String newValueJson = newObj != null ? objectMapper.writeValueAsString(newObj) : null;

            String username = "system";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                username = auth.getName();
            }

            AuditLog log = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .operation(operation)
                    .oldValue(oldValueJson)
                    .newValue(newValueJson)
                    .modifiedBy(username)
                    .build();

            auditLogRepository.save(log);
            logger.info("Audit log saved: {} on {} ID: {}", operation, entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to save audit log for {} {} ID: {}", operation, entityType, entityId, e);
        }
    }

    // Deep copy hack to prevent Hibernate reference issues when capturing 'oldState'
    private RegulatoryChange cloneChange(RegulatoryChange original) {
        try {
            String json = objectMapper.writeValueAsString(original);
            return objectMapper.readValue(json, RegulatoryChange.class);
        } catch (Exception e) {
            return original;
        }
    }
}
