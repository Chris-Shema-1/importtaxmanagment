package com.importtax.server.dao.impl;

import com.importtax.server.dao.PaymentDao;
import com.importtax.server.model.Payment;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;

public class PaymentDaoImpl extends GenericDaoImpl<Payment> implements PaymentDao {

    public PaymentDaoImpl() {
        super(Payment.class);
    }

    public PaymentDaoImpl(SessionFactory sessionFactory) {
        super(Payment.class, sessionFactory);
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) {
        return executeReadOnly(session -> {
            TypedQuery<Payment> query = session.createQuery(
                    "select p from Payment p where p.paymentStatus = :paymentStatus order by p.paymentDate desc",
                    Payment.class);
            query.setParameter("paymentStatus", paymentStatus);
            return query.getResultList();
        }, "findByPaymentStatus");
    }
}
