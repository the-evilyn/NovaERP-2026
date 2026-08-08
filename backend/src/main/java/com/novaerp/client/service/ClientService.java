package com.novaerp.client.service;

import com.novaerp.client.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    Page<ClientDTO> getClients(Pageable pageable, String search);
    ClientDTO getClientById(Long id);
    ClientDTO getClientByCode(String code);
    ClientDTO createClient(ClientDTO dto);
    List<ClientDTO> createClients(List<ClientDTO> dtos);
    ClientDTO updateClient(Long id, ClientDTO dto);
    void deleteClient(Long id);
    void deleteClients(List<Long> ids);
}
