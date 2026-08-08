package com.novaerp.audit.repository;

import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entite IS NULL OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :entite, '%'))) AND " +
           "(:search IS NULL OR LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> searchAuditLogs(
            @Param("action") AuditAction action,
            @Param("entite") String entite,
            @Param("search") String search,
            Pageable pageable
    );

    List<AuditLog> findAllByOrderByTimestampDesc();
}
