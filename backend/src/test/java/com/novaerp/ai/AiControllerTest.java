package com.novaerp.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.ai.controller.AiController;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.service.AiAssistantService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiAssistantService aiAssistantService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
