package com.novaerp.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageDTO {

    private Long id;

    @JsonProperty("role")
    @Builder.Default
    private String role = "user"; // 'user' | 'assistant' | 'system'

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    @JsonAlias({"timestamp", "createdAt", "date"})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("metadata")
    private MessageMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageMetadata {
        private String intent;
        private Map<String, Object> entities;
        private Double confidence;
        private List<ActionSuggestion> actions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionSuggestion {
        private String label;
        private String action;
        private Object payload;
    }
}
