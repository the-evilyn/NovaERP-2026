package com.novaerp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.client.controller.ClientController;
import com.novaerp.client.dto.ClientDTO;
import com.novaerp.client.service.ClientService;
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
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SpringDataJacksonConfiguration.PageModule(
                    new SpringDataWebSettings(PageSerializationMode.DIRECT)));

    @InjectMocks
    private ClientController clientController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

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
