package com.novaerp.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.product.controller.ProductController;
import com.novaerp.product.dto.ProductDTO;
import com.novaerp.product.service.ProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetProductsEndpoint() throws Exception {
        ProductDTO product = ProductDTO.builder()
                .id(1L).nom("Huile de table 5L").reference("HUI-005")
                .prixAchat(BigDecimal.valueOf(85)).prixVente(BigDecimal.valueOf(105))
                .quantiteStock(BigDecimal.valueOf(120)).seuilMinimum(BigDecimal.valueOf(30))
                .categorie("Alimentation").build();

        when(productService.getProducts(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(product)));

        mockMvc.perform(get("/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Huile de table 5L"))
                .andExpect(jsonPath("$.content[0].reference").value("HUI-005"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetProductByIdEndpoint() throws Exception {
        ProductDTO product = ProductDTO.builder().id(1L).nom("Huile de table 5L").reference("HUI-005").build();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Huile de table 5L"))
                .andExpect(jsonPath("$.reference").value("HUI-005"));
    }

    @Test
    void testCreateSingleProductEndpoint() throws Exception {
        ProductDTO input = ProductDTO.builder().nom("Sucre 2kg").reference("SUC-002")
                .prixAchat(BigDecimal.valueOf(18)).prixVente(BigDecimal.valueOf(24)).build();
        ProductDTO output = ProductDTO.builder().id(2L).nom("Sucre 2kg").reference("SUC-002")
                .prixAchat(BigDecimal.valueOf(18)).prixVente(BigDecimal.valueOf(24)).build();

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(output);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nom").value("Sucre 2kg"));
    }
}
