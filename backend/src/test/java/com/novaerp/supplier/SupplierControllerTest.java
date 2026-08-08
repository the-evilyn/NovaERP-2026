package com.novaerp.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import com.novaerp.supplier.controller.SupplierController;
import com.novaerp.supplier.dto.SupplierDTO;
import com.novaerp.supplier.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

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
    void testGetSuppliersEndpoint() throws Exception {
        SupplierDTO supplier = SupplierDTO.builder()
                .id(1L)
                .code("FRN-0001")
                .nom("Huileries du Souss SA")
                .email("contact@huileries-souss.ma")
                .build();

        when(supplierService.getSuppliers(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(supplier)));

        mockMvc.perform(get("/suppliers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Huileries du Souss SA"))
                .andExpect(jsonPath("$.content[0].code").value("FRN-0001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCreateSingleSupplierEndpoint() throws Exception {
        SupplierDTO input = SupplierDTO.builder()
                .nom("Cosumar Raffinerie SA")
                .email("commercial@cosumar.co.ma")
                .build();

        SupplierDTO output = SupplierDTO.builder()
                .id(2L)
                .code("FRN-0002")
                .nom("Cosumar Raffinerie SA")
                .email("commercial@cosumar.co.ma")
                .build();

        when(supplierService.createSupplier(any(SupplierDTO.class))).thenReturn(output);

        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nom").value("Cosumar Raffinerie SA"));
    }
}
