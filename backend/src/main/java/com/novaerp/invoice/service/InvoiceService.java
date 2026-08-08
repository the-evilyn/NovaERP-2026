package com.novaerp.invoice.service;

import com.novaerp.invoice.dto.InvoiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {
    Page<InvoiceDTO> getInvoices(Pageable pageable, String search);
    InvoiceDTO getInvoiceById(Long id);
    InvoiceDTO createInvoice(InvoiceDTO dto);
    List<InvoiceDTO> createInvoices(List<InvoiceDTO> dtos);
    InvoiceDTO createInvoiceFromSale(Long saleId);
    InvoiceDTO updateInvoice(Long id, InvoiceDTO dto);
    void deleteInvoice(Long id);
    void deleteInvoices(List<Long> ids);
}
