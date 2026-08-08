package com.novaerp.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.product.controller.ProductController;
import com.novaerp.product.dto.ProductDTO;
import com.novaerp.product.service.ProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

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
    void testGetProductsEndpoint() throws Exception {
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .nom("Huile de table 5L")
                .reference("HUI-005")
                .prixAchat(BigDecimal.valueOf(85))
                .prixVente(BigDecimal.valueOf(105))
                .quantiteStock(BigDecimal.valueOf(120))
                .seuilMinimum(BigDecimal.valueOf(30))
                .categorie("Alimentation")
                .build();

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
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .nom("Huile de table 5L")
                .reference("HUI-005")
                .build();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Huile de table 5L"))
                .andExpect(jsonPath("$.reference").value("HUI-005"));
    }

    @Test
    void testCreateSingleProductEndpoint() throws Exception {
        ProductDTO input = ProductDTO.builder()
                .nom("Sucre 2kg")
                .reference("SUC-002")
                .prixAchat(BigDecimal.valueOf(18))
                .prixVente(BigDecimal.valueOf(24))
                .build();

        ProductDTO output = ProductDTO.builder()
                .id(2L)
                .nom("Sucre 2kg")
                .reference("SUC-002")
                .prixAchat(BigDecimal.valueOf(18))
                .prixVente(BigDecimal.valueOf(24))
                .build();

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(output);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nom").value("Sucre 2kg"));
    }
}
