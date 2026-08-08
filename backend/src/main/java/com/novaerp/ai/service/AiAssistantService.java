package com.novaerp.ai.service;

import com.novaerp.ai.dto.AiAnomalyDTO;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.entity.AnomalyStatus;

import java.util.List;

public interface AiAssistantService {
    AiMessageDTO chat(AiChatRequestDTO request);
    List<AiPredictionDTO> getStockPredictions();
    List<AiAnomalyDTO> getAnomalies();
    void resolveAnomaly(Long id, AnomalyStatus status);
}
