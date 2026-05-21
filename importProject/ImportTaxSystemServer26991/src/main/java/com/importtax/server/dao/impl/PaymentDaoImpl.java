package com.importtax.server.dao.impl;

import com.importtax.server.dao.PaymentDao;
import com.importtax.server.model.Payment;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
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
    public List<Payment> findAll() {
        return executeReadOnly(session -> session.createQuery(
                        "select p from Payment p left join fetch p.invoice "
                                + "order by p.paymentDate desc, p.paymentId desc",
                        Payment.class)
                .getResultList(), "findAll");
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return executeReadOnly(session -> {
            List<Payment> list = session.createQuery(
                            "select p from Payment p left join fetch p.invoice where p.paymentId = :id",
                            Payment.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }, "findById");
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) {
        return executeReadOnly(session -> {
            TypedQuery<Payment> query = session.createQuery(
                    "select p from Payment p left join fetch p.invoice "
                            + "where p.paymentStatus = :paymentStatus order by p.paymentDate desc",
                    Payment.class);
            query.setParameter("paymentStatus", paymentStatus);
            return query.getResultList();
        }, "findByPaymentStatus");
    }

    @Override
    public Optional<Payment> findByInvoiceId(Long invoiceId) {
        return executeReadOnly(session -> {
            List<Payment> list = session.createQuery(
                            "select p from Payment p left join fetch p.invoice "
                                    + "where p.invoice.invoiceId = :invoiceId",
                            Payment.class)
                    .setParameter("invoiceId", invoiceId)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }, "findByInvoiceId");
    }

    @Override
    public long countPayments() {
        return executeReadOnly(session -> session.createQuery(
                "select count(p) from Payment p", Long.class).getSingleResult(), "countPayments");
    }

    @Override
    public long countByPaymentStatus(String paymentStatus) {
        return executeReadOnly(session -> session.createQuery(
                        "select count(p) from Payment p where upper(p.paymentStatus) = upper(:status)", Long.class)
                .setParameter("status", paymentStatus)
                .getSingleResult(), "countByPaymentStatus");
    }

    @Override
    public BigDecimal sumAmountByPaymentStatus(String paymentStatus) {
        return executeReadOnly(session -> {
            BigDecimal sum = session.createQuery(
                            "select coalesce(sum(p.amountPaid), 0) from Payment p "
                                    + "where upper(p.paymentStatus) = upper(:status)",
                            BigDecimal.class)
                    .setParameter("status", paymentStatus)
                    .getSingleResult();
            return sum != null ? sum : BigDecimal.ZERO;
        }, "sumAmountByPaymentStatus");
    }

    @Override
    public List<Payment> findRecentPayments(int limit) {
        int max = Math.max(1, Math.min(limit, 20));
        return executeReadOnly(session -> session.createQuery(
                        "select p from Payment p left join fetch p.invoice "
                                + "order by p.paymentDate desc, p.paymentId desc",
                        Payment.class)
                .setMaxResults(max)
                .getResultList(), "findRecentPayments");
    }
}
