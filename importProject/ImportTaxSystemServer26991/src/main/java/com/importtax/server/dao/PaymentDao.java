package com.importtax.server.dao;

import com.importtax.server.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentDao extends GenericDao<Payment> {

    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findAllPayments();

    Optional<Payment> findPaymentById(Long paymentId);

    Optional<Payment> findByInvoiceId(Long invoiceId);
}
