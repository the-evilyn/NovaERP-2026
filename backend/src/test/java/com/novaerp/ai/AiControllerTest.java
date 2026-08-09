package com.novaerp.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.ai.controller.AiController;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.service.AiAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AiAssistantService aiAssistantService;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(aiController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testChatEndpoint() throws Exception {
        AiChatRequestDTO request = AiChatRequestDTO.builder().content("Bonjour").build();
        AiMessageDTO response = AiMessageDTO.builder()
                .role("assistant")
                .content("Bonjour, comment puis-je vous aider ?")
                .timestamp(LocalDateTime.now())
                .build();

        when(aiAssistantService.chat(any(AiChatRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("Bonjour, comment puis-je vous aider ?"));
    }

    @Test
    void testGetStockPredictionsEndpoint() throws Exception {
        AiPredictionDTO prediction = AiPredictionDTO.builder()
                .produitId(1L)
                .produitNom("Riz 5kg")
                .recommandation("COMMANDER_URGENT")
                .build();

        when(aiAssistantService.getStockPredictions()).thenReturn(List.of(prediction));

        mockMvc.perform(get("/ai/stock-predictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produitNom").value("Riz 5kg"))
                .andExpect(jsonPath("$[0].recommandation").value("COMMANDER_URGENT"));
    }
}
