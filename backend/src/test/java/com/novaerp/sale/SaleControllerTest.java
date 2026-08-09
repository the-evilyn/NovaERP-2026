package com.novaerp.sale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.sale.controller.SaleController;
import com.novaerp.sale.dto.SaleDTO;
import com.novaerp.sale.dto.SaleItemDTO;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaleService saleService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private SaleController saleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(saleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetSalesEndpoint() throws Exception {
        SaleDTO sale = SaleDTO.builder()
                .id(1L).reference("VTE-2026-001").clientNom("LabelVie SA")
                .totalTTC(BigDecimal.valueOf(27600.0)).statut(SaleStatus.LIVREE).build();

        when(saleService.getSales(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(sale)));

        mockMvc.perform(get("/sales?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("VTE-2026-001"))
                .andExpect(jsonPath("$.content[0].clientNom").value("LabelVie SA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCreateSaleEndpoint() throws Exception {
        SaleDTO input = SaleDTO.builder()
                .clientId(1L)
                .items(List.of(SaleItemDTO.builder().produitId(1L)
                        .quantite(BigDecimal.valueOf(10)).prixUnitaire(BigDecimal.valueOf(115.0)).build()))
                .build();

        SaleDTO output = SaleDTO.builder()
                .id(1L).reference("VTE-2026-001").clientId(1L)
                .statut(SaleStatus.COMMANDE).date(LocalDate.now()).build();

        when(saleService.createSale(any(SaleDTO.class))).thenReturn(output);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("VTE-2026-001"));
    }
}
