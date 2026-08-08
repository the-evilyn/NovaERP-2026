package com.novaerp.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.purchase.controller.PurchaseController;
import com.novaerp.purchase.dto.PurchaseDTO;
import com.novaerp.purchase.dto.PurchaseItemDTO;
import com.novaerp.purchase.entity.PurchaseStatus;
import com.novaerp.purchase.service.PurchaseService;
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

@WebMvcTest(PurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseService purchaseService;

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
    void testGetPurchasesEndpoint() throws Exception {
        PurchaseDTO po = PurchaseDTO.builder()
                .id(1L)
                .reference("ACH-2026-001")
                .fournisseurNom("Huileries du Souss")
                .totalTTC(BigDecimal.valueOf(10200.0))
                .statut(PurchaseStatus.EN_ATTENTE)
                .build();

        when(purchaseService.getPurchases(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(po)));

        mockMvc.perform(get("/purchases?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("ACH-2026-001"))
                .andExpect(jsonPath("$.content[0].fournisseurNom").value("Huileries du Souss"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCreatePurchaseEndpoint() throws Exception {
        PurchaseDTO input = PurchaseDTO.builder()
                .fournisseurId(1L)
                .items(List.of(
                        PurchaseItemDTO.builder()
                                .produitId(1L)
                                .quantite(BigDecimal.valueOf(10))
                                .prixUnitaire(BigDecimal.valueOf(85.0))
                                .build()
                ))
                .build();

        PurchaseDTO output = PurchaseDTO.builder()
                .id(1L)
                .reference("ACH-2026-001")
                .fournisseurId(1L)
                .statut(PurchaseStatus.EN_ATTENTE)
                .date(LocalDate.now())
                .build();

        when(purchaseService.createPurchase(any(PurchaseDTO.class))).thenReturn(output);

        mockMvc.perform(post("/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("ACH-2026-001"));
    }
}
