package com.novaerp.ai.controller;

import com.novaerp.ai.dto.AiAnomalyDTO;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.entity.AnomalyStatus;
import com.novaerp.ai.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Assistant & Decision Support", description = "Endpoints for conversational AI copilot, inventory predictive runout analysis, and fraud/anomaly detection")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with NovaAI assistant", description = "Executes NLP heuristics and real-time database contextual analysis")
    public ResponseEntity<AiMessageDTO> chat(@Valid @RequestBody AiChatRequestDTO request) {
        return ResponseEntity.ok(aiAssistantService.chat(request));
    }

    @GetMapping("/stock-predictions")
    @Operation(summary = "Get AI stock predictive analytics", description = "Calculates days until depletion, runout projections, and purchase reorder recommendations")
    public ResponseEntity<List<AiPredictionDTO>> getStockPredictions() {
        return ResponseEntity.ok(aiAssistantService.getStockPredictions());
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get detected ERP anomalies", description = "Lists price deviations, duplicate order spikes, and lead time delays")
    public ResponseEntity<List<AiAnomalyDTO>> getAnomalies() {
        return ResponseEntity.ok(aiAssistantService.getAnomalies());
    }

    @RequestMapping(value = "/anomalies/{id}/resolve", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Resolve or ignore anomaly", description = "Updates anomaly resolution status")
    public ResponseEntity<Void> resolveAnomaly(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String action
    ) {
        String statusStr = (body != null && body.containsKey("action")) ? body.get("action") : (action != null ? action : "RESOLU");
        AnomalyStatus status = "IGNORE".equalsIgnoreCase(statusStr) ? AnomalyStatus.IGNORE : AnomalyStatus.RESOLU;
        aiAssistantService.resolveAnomaly(id, status);
        return ResponseEntity.ok().build();
    }
}
