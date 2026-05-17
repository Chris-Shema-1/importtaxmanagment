package com.importtax.server.dao.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.model.Invoice;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class InvoiceDaoImpl extends GenericDaoImpl<Invoice> implements InvoiceDao {

    public InvoiceDaoImpl() {
        super(Invoice.class);
    }

    public InvoiceDaoImpl(SessionFactory sessionFactory) {
        super(Invoice.class, sessionFactory);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return executeReadOnly(session -> {
            TypedQuery<Invoice> query = session.createQuery(
                    "select i from Invoice i left join fetch i.payment where i.invoiceNumber = :invoiceNumber",
                    Invoice.class);
            query.setParameter("invoiceNumber", invoiceNumber);
            return query.getResultStream().findFirst();
        }, "findByInvoiceNumber");
    }

    @Override
    public List<Invoice> findAllInvoices() {
        return executeReadOnly(session -> session.createQuery(
                "select i from Invoice i left join fetch i.payment order by i.issueDate desc, i.invoiceId desc",
                Invoice.class).getResultList(), "findAllInvoices");
    }

    @Override
    public Optional<Invoice> findInvoiceById(Long invoiceId) {
        return executeReadOnly(session -> {
            TypedQuery<Invoice> query = session.createQuery(
                    "select i from Invoice i left join fetch i.payment where i.invoiceId = :invoiceId",
                    Invoice.class);
            query.setParameter("invoiceId", invoiceId);
            return query.getResultStream().findFirst();
        }, "findInvoiceById");
    }
}
