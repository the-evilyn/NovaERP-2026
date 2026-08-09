package com.novaerp.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.invoice.controller.InvoiceController;
import com.novaerp.invoice.dto.InvoiceDTO;
import com.novaerp.invoice.dto.InvoiceItemDTO;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.service.InvoiceService;
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
class InvoiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvoiceService invoiceService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetInvoicesEndpoint() throws Exception {
        InvoiceDTO invoice = InvoiceDTO.builder()
                .id(1L).numero("FAC-2026-001").clientNom("LabelVie SA")
                .totalTTC(BigDecimal.valueOf(27600.0)).statut(InvoiceStatus.VALIDEE).build();

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
                .lignes(List.of(InvoiceItemDTO.builder().productId(1L)
                        .quantite(BigDecimal.valueOf(10)).prixUnitaire(BigDecimal.valueOf(115.0)).build()))
                .build();

        InvoiceDTO output = InvoiceDTO.builder()
                .id(1L).numero("FAC-2026-001").clientId(1L)
                .statut(InvoiceStatus.VALIDEE).date(LocalDate.now()).build();

        when(invoiceService.createInvoice(any(InvoiceDTO.class))).thenReturn(output);

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("FAC-2026-001"));
    }
}
