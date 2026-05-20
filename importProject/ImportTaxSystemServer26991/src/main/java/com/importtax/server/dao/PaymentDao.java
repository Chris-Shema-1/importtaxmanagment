package com.importtax.server.dao;

import com.importtax.server.model.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentDao extends GenericDao<Payment> {

    List<Payment> findByPaymentStatus(String paymentStatus);

    Optional<Payment> findByInvoiceId(Long invoiceId);

    long countPayments();

    long countByPaymentStatus(String paymentStatus);

    BigDecimal sumAmountByPaymentStatus(String paymentStatus);

    List<Payment> findRecentPayments(int limit);
}
