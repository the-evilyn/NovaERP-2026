package com.novaerp.supplier.service;

import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.supplier.dto.SupplierDTO;
import com.novaerp.supplier.entity.Supplier;
import com.novaerp.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getSuppliers(Pageable pageable, String search) {
        Page<Supplier> page = StringUtils.hasText(search)
                ? supplierRepository.searchSuppliers(search, pageable)
                : supplierRepository.findAll(pageable);
        return page.map(SupplierDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return SupplierDTO.fromEntity(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierByCode(String code) {
        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with code: " + code));
        return SupplierDTO.fromEntity(supplier);
    }

    @Override
    @Transactional
    public SupplierDTO createSupplier(SupplierDTO dto) {
        log.info("Creating supplier: {}", dto.getNom());

        String code = StringUtils.hasText(dto.getCode())
                ? dto.getCode().trim().toUpperCase()
                : generateSupplierCode();

        if (supplierRepository.existsByCode(code)) {
            throw new ResourceAlreadyExistsException("Supplier with code " + code + " already exists");
        }

        Supplier supplier = Supplier.builder()
                .code(code)
                .name(dto.getNom().trim())
                .email(dto.getEmail())
                .phone(dto.getTelephone())
                .address(dto.getAdresse())
                .ice(dto.getIce())
                .taxId(dto.getTaxId())
                .contactName(dto.getContactName())
                .city(dto.getCity())
                .country(dto.getCountry() != null ? dto.getCountry() : "Morocco")
                .paymentTerms(dto.getPaymentTerms() != null ? dto.getPaymentTerms() : 30)
                .active(dto.isActive())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<SupplierDTO> createSuppliers(List<SupplierDTO> dtos) {
        log.info("Batch creating {} suppliers", dtos.size());
        List<SupplierDTO> results = new ArrayList<>();
        for (SupplierDTO dto : dtos) {
            results.add(createSupplier(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO dto) {
        log.info("Updating supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (StringUtils.hasText(dto.getNom())) {
            supplier.setName(dto.getNom().trim());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            supplier.setEmail(dto.getEmail().trim());
        }
        if (StringUtils.hasText(dto.getTelephone())) {
            supplier.setPhone(dto.getTelephone().trim());
        }
        if (StringUtils.hasText(dto.getAdresse())) {
            supplier.setAddress(dto.getAdresse().trim());
        }
        if (StringUtils.hasText(dto.getIce())) {
            supplier.setIce(dto.getIce().trim());
        }
        if (dto.getPaymentTerms() != null) {
            supplier.setPaymentTerms(dto.getPaymentTerms());
        }
        supplier.setActive(dto.isActive());

        Supplier updated = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id: {}", id);
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteSuppliers(List<Long> ids) {
        log.info("Batch deleting suppliers: {}", ids);
        supplierRepository.deleteAllById(ids);
    }

    private String generateSupplierCode() {
        long count = supplierRepository.count() + 1;
        return String.format("FRN-%04d", count);
    }
}
