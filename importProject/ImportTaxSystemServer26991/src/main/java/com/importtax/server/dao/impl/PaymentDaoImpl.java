package com.importtax.server.dao.impl;

import com.importtax.server.dao.PaymentDao;
import com.importtax.server.model.Payment;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

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
                    "select p from Payment p join fetch p.invoice where p.paymentStatus = :paymentStatus "
                            + "order by p.paymentDate desc",
                    Payment.class);
            query.setParameter("paymentStatus", paymentStatus);
            return query.getResultList();
        }, "findByPaymentStatus");
    }

    @Override
    public List<Payment> findAllPayments() {
        return executeReadOnly(session -> session.createQuery(
                "select p from Payment p join fetch p.invoice order by p.paymentDate desc, p.paymentId desc",
                Payment.class).getResultList(), "findAllPayments");
    }

    @Override
    public Optional<Payment> findPaymentById(Long paymentId) {
        return executeReadOnly(session -> {
            TypedQuery<Payment> query = session.createQuery(
                    "select p from Payment p join fetch p.invoice where p.paymentId = :paymentId",
                    Payment.class);
            query.setParameter("paymentId", paymentId);
            return query.getResultStream().findFirst();
        }, "findPaymentById");
    }

    @Override
    public Optional<Payment> findByInvoiceId(Long invoiceId) {
        return executeReadOnly(session -> {
            TypedQuery<Payment> query = session.createQuery(
                    "select p from Payment p join fetch p.invoice where p.invoice.invoiceId = :invoiceId",
                    Payment.class);
            query.setParameter("invoiceId", invoiceId);
            return query.getResultStream().findFirst();
        }, "findByInvoiceId");
    }
}
