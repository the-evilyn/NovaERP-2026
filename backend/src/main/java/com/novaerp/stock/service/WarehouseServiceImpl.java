package com.novaerp.stock.service;

import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.stock.dto.WarehouseDTO;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return WarehouseDTO.fromEntity(warehouse);
    }

    @Override
    @Transactional
    public WarehouseDTO createWarehouse(WarehouseDTO dto) {
        log.info("Creating warehouse: {}", dto.getName());

        if (warehouseRepository.existsByCode(dto.getCode())) {
            throw new ResourceAlreadyExistsException("Warehouse with code " + dto.getCode() + " already exists");
        }

        Warehouse warehouse = Warehouse.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .address(dto.getAddress())
                .city(dto.getCity())
                .country(dto.getCountry() != null ? dto.getCountry() : "Morocco")
                .active(dto.isActive())
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        return WarehouseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO dto) {
        log.info("Updating warehouse with id: {}", id);

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        if (dto.getName() != null) {
            warehouse.setName(dto.getName().trim());
        }
        if (dto.getAddress() != null) {
            warehouse.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            warehouse.setCity(dto.getCity());
        }
        if (dto.getCountry() != null) {
            warehouse.setCountry(dto.getCountry());
        }
        warehouse.setActive(dto.isActive());

        Warehouse updated = warehouseRepository.save(warehouse);
        return WarehouseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        log.info("Deleting warehouse with id: {}", id);
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse not found with id: " + id);
        }
        warehouseRepository.deleteById(id);
    }
}
