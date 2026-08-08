package com.novaerp.ai.repository;

import com.novaerp.ai.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
