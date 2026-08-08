package com.novaerp.supplier.service;

import com.novaerp.supplier.dto.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierService {
    Page<SupplierDTO> getSuppliers(Pageable pageable, String search);
    SupplierDTO getSupplierById(Long id);
    SupplierDTO getSupplierByCode(String code);
    SupplierDTO createSupplier(SupplierDTO dto);
    List<SupplierDTO> createSuppliers(List<SupplierDTO> dtos);
    SupplierDTO updateSupplier(Long id, SupplierDTO dto);
    void deleteSupplier(Long id);
    void deleteSuppliers(List<Long> ids);
}
