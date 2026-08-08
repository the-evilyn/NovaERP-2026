package com.novaerp.client.service;

import com.novaerp.client.dto.ClientDTO;
import com.novaerp.client.entity.Client;
import com.novaerp.client.entity.ClientStatus;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDTO> getClients(Pageable pageable, String search) {
        if (StringUtils.hasText(search)) {
            return clientRepository.searchClients(search, pageable).map(ClientDTO::fromEntity);
        }
        return clientRepository.findAll(pageable).map(ClientDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return ClientDTO.fromEntity(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientByCode(String code) {
        Client client = clientRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with code: " + code));
        return ClientDTO.fromEntity(client);
    }

    @Override
    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        log.info("Creating new client: {}", dto.getNom());

        String clientCode = StringUtils.hasText(dto.getCode())
                ? dto.getCode()
                : generateClientCode();

        if (clientRepository.existsByCode(clientCode)) {
            throw new ResourceAlreadyExistsException("Client with code " + clientCode + " already exists");
        }

        if (StringUtils.hasText(dto.getEmail()) && clientRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Client with email " + dto.getEmail() + " already exists");
        }

        Client client = Client.builder()
                .code(clientCode)
                .name(dto.getNom())
                .companyName(StringUtils.hasText(dto.getCompanyName()) ? dto.getCompanyName() : dto.getNom())
                .email(dto.getEmail())
                .phone(dto.getTelephone())
                .address(dto.getAdresse())
                .city(dto.getCity())
                .country(StringUtils.hasText(dto.getCountry()) ? dto.getCountry() : "Morocco")
                .taxNumber(dto.getTaxNumber())
                .creditLimit(dto.getCreditLimit() != null ? dto.getCreditLimit() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : ClientStatus.ACTIVE)
                .build();

        Client saved = clientRepository.save(client);
        return ClientDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<ClientDTO> createClients(List<ClientDTO> dtos) {
        log.info("Batch creating {} clients", dtos.size());
        List<ClientDTO> results = new ArrayList<>();
        for (ClientDTO dto : dtos) {
            results.add(createClient(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO dto) {
        log.info("Updating client with id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (StringUtils.hasText(dto.getNom())) {
            client.setName(dto.getNom());
        }
        if (dto.getCompanyName() != null) {
            client.setCompanyName(dto.getCompanyName());
        }
        if (dto.getEmail() != null) {
            client.setEmail(dto.getEmail());
        }
        if (dto.getTelephone() != null) {
            client.setPhone(dto.getTelephone());
        }
        if (dto.getAdresse() != null) {
            client.setAddress(dto.getAdresse());
        }
        if (dto.getCity() != null) {
            client.setCity(dto.getCity());
        }
        if (dto.getCountry() != null) {
            client.setCountry(dto.getCountry());
        }
        if (dto.getTaxNumber() != null) {
            client.setTaxNumber(dto.getTaxNumber());
        }
        if (dto.getCreditLimit() != null) {
            client.setCreditLimit(dto.getCreditLimit());
        }
        if (dto.getStatus() != null) {
            client.setStatus(dto.getStatus());
        }

        Client updated = clientRepository.save(client);
        return ClientDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        log.info("Deleting client with id: {}", id);
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteClients(List<Long> ids) {
        log.info("Batch deleting clients: {}", ids);
        clientRepository.deleteAllById(ids);
    }

    private String generateClientCode() {
        long count = clientRepository.count() + 1;
        return String.format("CLI-%04d", count);
    }
}
