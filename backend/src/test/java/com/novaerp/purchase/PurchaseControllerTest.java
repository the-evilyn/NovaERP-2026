package com.novaerp.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.purchase.controller.PurchaseController;
import com.novaerp.purchase.dto.PurchaseDTO;
import com.novaerp.purchase.dto.PurchaseItemDTO;
import com.novaerp.purchase.entity.PurchaseStatus;
import com.novaerp.purchase.service.PurchaseService;
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
class PurchaseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PurchaseService purchaseService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private PurchaseController purchaseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetPurchasesEndpoint() throws Exception {
        PurchaseDTO po = PurchaseDTO.builder()
                .id(1L).reference("ACH-2026-001").fournisseurNom("Huileries du Souss")
                .totalTTC(BigDecimal.valueOf(10200.0)).statut(PurchaseStatus.EN_ATTENTE).build();

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
                .items(List.of(PurchaseItemDTO.builder().produitId(1L)
                        .quantite(BigDecimal.valueOf(10)).prixUnitaire(BigDecimal.valueOf(85.0)).build()))
                .build();

        PurchaseDTO output = PurchaseDTO.builder()
                .id(1L).reference("ACH-2026-001").fournisseurId(1L)
                .statut(PurchaseStatus.EN_ATTENTE).date(LocalDate.now()).build();

        when(purchaseService.createPurchase(any(PurchaseDTO.class))).thenReturn(output);

        mockMvc.perform(post("/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("ACH-2026-001"));
    }
}
