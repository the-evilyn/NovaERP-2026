package com.novaerp.ai.repository;

import com.novaerp.ai.entity.AiAnomaly;
import com.novaerp.ai.entity.AnomalyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAnomalyRepository extends JpaRepository<AiAnomaly, Long> {
    List<AiAnomaly> findAllByOrderByDetectionDateDesc();
    List<AiAnomaly> findByStatusOrderByDetectionDateDesc(AnomalyStatus status);
}
