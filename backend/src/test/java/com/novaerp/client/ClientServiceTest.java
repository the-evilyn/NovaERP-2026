package com.novaerp.client;

import com.novaerp.client.dto.ClientDTO;
import com.novaerp.client.entity.Client;
import com.novaerp.client.entity.ClientStatus;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.client.service.ClientServiceImpl;
import com.novaerp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client sampleClient;

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder()
                .id(1L)
                .code("CLI-0001")
                .name("Société Atlas Distribution")
                .companyName("Société Atlas Distribution")
                .email("contact@atlas-dist.ma")
                .phone("0522334455")
                .address("Zone industrielle, Casablanca")
                .status(ClientStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetClients() {
        Page<Client> clientPage = new PageImpl<>(List.of(sampleClient));
        when(clientRepository.findAll(any(PageRequest.class))).thenReturn(clientPage);

        Page<ClientDTO> result = clientService.getClients(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Société Atlas Distribution", result.getContent().get(0).getNom());
    }

    @Test
    void testGetClientById() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));

        ClientDTO result = clientService.getClientById(1L);

        assertNotNull(result);
        assertEquals("CLI-0001", result.getCode());
        assertEquals("Société Atlas Distribution", result.getNom());
    }

    @Test
    void testCreateClient() {
        ClientDTO input = ClientDTO.builder()
                .nom("Nouveau Client")
                .email("new@client.ma")
                .telephone("0600000000")
                .adresse("Rabat")
                .build();

        when(clientRepository.count()).thenReturn(1L);
        when(clientRepository.existsByCode(anyString())).thenReturn(false);
        when(clientRepository.existsByEmail("new@client.ma")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> {
            Client c = i.getArgument(0);
            c.setId(2L);
            return c;
        });

        ClientDTO result = clientService.createClient(input);

        assertNotNull(result);
        assertEquals("Nouveau Client", result.getNom());
        assertEquals("CLI-0002", result.getCode());
    }

    @Test
    void testDeleteClientNotFoundThrowsException() {
        when(clientRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteClient(99L));
    }
}
