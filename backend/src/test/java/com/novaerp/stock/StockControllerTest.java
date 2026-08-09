package com.novaerp.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.stock.controller.StockController;
import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockDTO;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(stockController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

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
                .productNom("Huile 5L")
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
