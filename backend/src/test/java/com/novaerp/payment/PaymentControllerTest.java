package com.novaerp.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.payment.controller.PaymentController;
import com.novaerp.payment.dto.PaymentDTO;
import com.novaerp.payment.entity.PaymentMethod;
import com.novaerp.payment.entity.PaymentStatus;
import com.novaerp.payment.service.PaymentService;
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

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

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
    void testGetPaymentsEndpoint() throws Exception {
        PaymentDTO payment = PaymentDTO.builder()
                .id(1L)
                .reference("REG-2026-001")
                .clientNom("LabelVie SA")
                .montant(BigDecimal.valueOf(27600.0))
                .statut(PaymentStatus.VALIDE)
                .build();

        when(paymentService.getPayments(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(payment)));

        mockMvc.perform(get("/payments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reference").value("REG-2026-001"))
                .andExpect(jsonPath("$.content[0].clientNom").value("LabelVie SA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCreatePaymentEndpoint() throws Exception {
        PaymentDTO input = PaymentDTO.builder()
                .factureId(1L)
                .montant(BigDecimal.valueOf(27600.0))
                .modePaiement(PaymentMethod.VIREMENT)
                .build();

        PaymentDTO output = PaymentDTO.builder()
                .id(1L)
                .reference("REG-2026-001")
                .montant(BigDecimal.valueOf(27600.0))
                .statut(PaymentStatus.VALIDE)
                .date(LocalDate.now())
                .build();

        when(paymentService.createPayment(any(PaymentDTO.class))).thenReturn(output);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("REG-2026-001"));
    }
}
