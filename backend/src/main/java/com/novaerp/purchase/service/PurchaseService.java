package com.novaerp.purchase.service;

import com.novaerp.purchase.dto.PurchaseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseService {
    Page<PurchaseDTO> getPurchases(Pageable pageable, String search);
    PurchaseDTO getPurchaseById(Long id);
    PurchaseDTO createPurchase(PurchaseDTO dto);
    List<PurchaseDTO> createPurchases(List<PurchaseDTO> dtos);
    PurchaseDTO updatePurchase(Long id, PurchaseDTO dto);
    PurchaseDTO receivePurchase(Long id);
    void deletePurchase(Long id);
    void deletePurchases(List<Long> ids);
}
