package com.novaerp.sale.service;

import com.novaerp.sale.dto.SaleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SaleService {
    Page<SaleDTO> getSales(Pageable pageable, String search);
    SaleDTO getSaleById(Long id);
    SaleDTO createSale(SaleDTO dto);
    List<SaleDTO> createSales(List<SaleDTO> dtos);
    SaleDTO updateSale(Long id, SaleDTO dto);
    SaleDTO deliverSale(Long id);
    void deleteSale(Long id);
    void deleteSales(List<Long> ids);
}
