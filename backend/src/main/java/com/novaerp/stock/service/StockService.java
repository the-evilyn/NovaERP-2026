package com.novaerp.stock.service;

import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockDTO;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.dto.StockTransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockService {
    List<StockDTO> getAllStock();
    List<StockDTO> getLowStockAlerts();
    Page<StockMovementDTO> getStockMovements(Pageable pageable);
    StockMovementDTO adjustStock(StockAdjustmentRequest request);
    StockMovementDTO transferStock(StockTransferRequest request);
}
