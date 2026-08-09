package com.novaerp.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.supplier.controller.SupplierController;
import com.novaerp.supplier.dto.SupplierDTO;
import com.novaerp.supplier.service.SupplierService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SupplierService supplierService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private SupplierController supplierController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetSuppliersEndpoint() throws Exception {
        SupplierDTO supplier = SupplierDTO.builder()
                .id(1L).code("FRN-0001").nom("Huileries du Souss SA").email("contact@huileries-souss.ma")
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
        SupplierDTO input = SupplierDTO.builder().nom("Cosumar Raffinerie SA").email("commercial@cosumar.co.ma").build();
        SupplierDTO output = SupplierDTO.builder().id(2L).code("FRN-0002").nom("Cosumar Raffinerie SA").email("commercial@cosumar.co.ma").build();

        when(supplierService.createSupplier(any(SupplierDTO.class))).thenReturn(output);

        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nom").value("Cosumar Raffinerie SA"));
    }
}
