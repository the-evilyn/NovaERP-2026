package com.novaerp.sale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.sale.controller.SaleController;
import com.novaerp.sale.dto.SaleDTO;
import com.novaerp.sale.dto.SaleItemDTO;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.service.SaleService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService saleService;

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
    void testGetSalesEndpoint() throws Exception {
        SaleDTO sale = SaleDTO.builder()
                .id(1L)
                .reference("VTE-2026-001")
                .clientNom("LabelVie SA")
                .totalTTC(BigDecimal.valueOf(27600.0))
                .statut(SaleStatus.LIVREE)
                .build();

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
                .items(List.of(
                        SaleItemDTO.builder()
                                .produitId(1L)
                                .quantite(BigDecimal.valueOf(10))
                                .prixUnitaire(BigDecimal.valueOf(115.0))
                                .build()
                ))
                .build();

        SaleDTO output = SaleDTO.builder()
                .id(1L)
                .reference("VTE-2026-001")
                .clientId(1L)
                .statut(SaleStatus.COMMANDE)
                .date(LocalDate.now())
                .build();

        when(saleService.createSale(any(SaleDTO.class))).thenReturn(output);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("VTE-2026-001"));
    }
}
