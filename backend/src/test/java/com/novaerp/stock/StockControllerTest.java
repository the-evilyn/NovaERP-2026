package com.novaerp.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import com.novaerp.stock.controller.StockController;
import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockDTO;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService stockService;

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
    void testGetAllStockEndpoint() throws Exception {
        StockDTO stock = StockDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Huile 5L")
                .sku("HUI-005")
                .quantityOnHand(BigDecimal.valueOf(100))
                .build();

        when(stockService.getAllStock()).thenReturn(List.of(stock));

        mockMvc.perform(get("/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produitNom").value("Huile 5L"))
                .andExpect(jsonPath("$[0].reference").value("HUI-005"));
    }

    @Test
    void testAdjustStockEndpoint() throws Exception {
        StockAdjustmentRequest req = StockAdjustmentRequest.builder()
                .productId(1L)
                .quantity(BigDecimal.valueOf(25))
                .type("ENTREE")
                .build();

        StockMovementDTO movement = StockMovementDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Huile 5L")
                .type("ENTREE")
                .quantite(BigDecimal.valueOf(25))
                .date(LocalDateTime.now())
                .build();

        when(stockService.adjustStock(any(StockAdjustmentRequest.class))).thenReturn(movement);

        mockMvc.perform(post("/stock/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ENTREE"))
                .andExpect(jsonPath("$.quantite").value(25));
    }
}
