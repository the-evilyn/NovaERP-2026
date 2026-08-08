package com.novaerp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.client.controller.ClientController;
import com.novaerp.client.dto.ClientDTO;
import com.novaerp.client.service.ClientService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

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
    void testGetClientsEndpoint() throws Exception {
        ClientDTO client = ClientDTO.builder()
                .id(1L)
                .code("CLI-0001")
                .nom("Société Atlas Distribution")
                .email("contact@atlas-dist.ma")
                .telephone("0522334455")
                .build();

        when(clientService.getClients(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(client)));

        mockMvc.perform(get("/clients?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Société Atlas Distribution"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetClientByIdEndpoint() throws Exception {
        ClientDTO client = ClientDTO.builder()
                .id(1L)
                .code("CLI-0001")
                .nom("Société Atlas Distribution")
                .build();

        when(clientService.getClientById(1L)).thenReturn(client);

        mockMvc.perform(get("/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Société Atlas Distribution"))
                .andExpect(jsonPath("$.code").value("CLI-0001"));
    }

    @Test
    void testCreateSingleClientEndpoint() throws Exception {
        ClientDTO input = ClientDTO.builder().nom("Nouveau Client").build();
        ClientDTO output = ClientDTO.builder().id(5L).code("CLI-0005").nom("Nouveau Client").build();

        when(clientService.createClient(any(ClientDTO.class))).thenReturn(output);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.nom").value("Nouveau Client"));
    }
}
