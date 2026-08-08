package com.novaerp.supplier;

import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.supplier.dto.SupplierDTO;
import com.novaerp.supplier.entity.Supplier;
import com.novaerp.supplier.repository.SupplierRepository;
import com.novaerp.supplier.service.SupplierServiceImpl;
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
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        sampleSupplier = Supplier.builder()
                .id(1L)
                .code("FRN-0001")
                .name("Huileries du Souss SA")
                .email("contact@huileries-souss.ma")
                .phone("0528221100")
                .address("Zone Industrielle Anza, Agadir")
                .ice("001589342000045")
                .active(true)
                .build();
    }

    @Test
    void testGetSuppliers() {
        Page<Supplier> page = new PageImpl<>(List.of(sampleSupplier));
        when(supplierRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SupplierDTO> result = supplierService.getSuppliers(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Huileries du Souss SA", result.getContent().get(0).getNom());
        assertEquals("FRN-0001", result.getContent().get(0).getCode());
    }

    @Test
    void testGetSupplierById() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));

        SupplierDTO result = supplierService.getSupplierById(1L);

        assertNotNull(result);
        assertEquals("FRN-0001", result.getCode());
        assertEquals("Huileries du Souss SA", result.getNom());
    }

    @Test
    void testCreateSupplier() {
        SupplierDTO input = SupplierDTO.builder()
                .nom("Cosumar Raffinerie SA")
                .email("commercial@cosumar.co.ma")
                .ice("002498112000078")
                .build();

        when(supplierRepository.count()).thenReturn(1L);
        when(supplierRepository.existsByCode("FRN-0002")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> {
            Supplier s = i.getArgument(0);
            s.setId(2L);
            return s;
        });

        SupplierDTO result = supplierService.createSupplier(input);

        assertNotNull(result);
        assertEquals("FRN-0002", result.getCode());
        assertEquals("Cosumar Raffinerie SA", result.getNom());
    }

    @Test
    void testDeleteSupplierNotFoundThrowsException() {
        when(supplierRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> supplierService.deleteSupplier(99L));
    }
}
