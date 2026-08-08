package com.novaerp.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDTO {

    @NotBlank(message = "Message content is required")
    @JsonProperty("content")
    private String content;

    @JsonProperty("conversationHistory")
    private List<AiMessageDTO> conversationHistory;
}
