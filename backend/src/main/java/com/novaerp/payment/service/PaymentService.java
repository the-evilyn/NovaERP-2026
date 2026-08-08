package com.novaerp.payment.service;

import com.novaerp.payment.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    Page<PaymentDTO> getPayments(Pageable pageable, String search);
    PaymentDTO getPaymentById(Long id);
    PaymentDTO createPayment(PaymentDTO dto);
    List<PaymentDTO> createPayments(List<PaymentDTO> dtos);
    PaymentDTO updatePayment(Long id, PaymentDTO dto);
    void deletePayment(Long id);
    void deletePayments(List<Long> ids);
}
