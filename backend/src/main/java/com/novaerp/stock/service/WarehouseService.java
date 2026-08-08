package com.novaerp.stock.service;

import com.novaerp.stock.dto.WarehouseDTO;

import java.util.List;

public interface WarehouseService {
    List<WarehouseDTO> getAllWarehouses();
    WarehouseDTO getWarehouseById(Long id);
    WarehouseDTO createWarehouse(WarehouseDTO dto);
    WarehouseDTO updateWarehouse(Long id, WarehouseDTO dto);
    void deleteWarehouse(Long id);
}
