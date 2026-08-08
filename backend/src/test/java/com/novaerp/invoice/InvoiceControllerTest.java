package com.novaerp.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.invoice.controller.InvoiceController;
import com.novaerp.invoice.dto.InvoiceDTO;
import com.novaerp.invoice.dto.InvoiceItemDTO;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.service.InvoiceService;
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

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

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
    void testGetInvoicesEndpoint() throws Exception {
        InvoiceDTO invoice = InvoiceDTO.builder()
                .id(1L)
                .reference("FAC-2026-001")
                .clientNom("LabelVie SA")
                .totalTTC(BigDecimal.valueOf(27600.0))
                .statut(InvoiceStatus.VALIDEE)
                .build();

        when(invoiceService.getInvoices(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(invoice)));

        mockMvc.perform(get("/invoices?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("FAC-2026-001"))
                .andExpect(jsonPath("$.content[0].clientNom").value("LabelVie SA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCreateInvoiceEndpoint() throws Exception {
        InvoiceDTO input = InvoiceDTO.builder()
                .clientId(1L)
                .items(List.of(
                        InvoiceItemDTO.builder()
                                .produitId(1L)
                                .quantite(BigDecimal.valueOf(10))
                                .prixUnitaire(BigDecimal.valueOf(115.0))
                                .build()
                ))
                .build();

        InvoiceDTO output = InvoiceDTO.builder()
                .id(1L)
                .reference("FAC-2026-001")
                .clientId(1L)
                .statut(InvoiceStatus.VALIDEE)
                .date(LocalDate.now())
                .build();

        when(invoiceService.createInvoice(any(InvoiceDTO.class))).thenReturn(output);

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("FAC-2026-001"));
    }
}
