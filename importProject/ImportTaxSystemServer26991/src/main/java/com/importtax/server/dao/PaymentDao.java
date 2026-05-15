package com.importtax.server.dao;

import com.importtax.server.model.Payment;

import java.util.List;

public interface PaymentDao extends GenericDao<Payment> {

    List<Payment> findByPaymentStatus(String paymentStatus);
}
