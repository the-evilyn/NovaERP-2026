package com.novaerp.alert.repository;

import com.novaerp.alert.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();
    List<Alert> findAllByOrderByCreatedAtDesc();
    Page<Alert> findByIsRead(Boolean isRead, Pageable pageable);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.isRead = false")
    int markAllAsRead();
}
