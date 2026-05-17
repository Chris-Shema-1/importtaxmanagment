package com.importtax.server.rmi.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class InvoiceServiceImpl extends AbstractRemoteCrudService<Invoice> implements InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceServiceImpl.class);
    private final InvoiceDao invoiceDao;

    public InvoiceServiceImpl() throws RemoteException {
        this(new InvoiceDaoImpl());
    }

    public InvoiceServiceImpl(InvoiceDao invoiceDao) throws RemoteException {
        super(invoiceDao, LOGGER, "InvoiceService");
        this.invoiceDao = invoiceDao;
    }

    @Override
    public Invoice save(Invoice entity) throws RemoteException {
        return execute("save", () -> {
            Invoice invoice = normalize(entity, false);
            if (invoiceDao.findByInvoiceNumber(invoice.getInvoiceNumber()).isPresent()) {
                throw new IllegalArgumentException("Invoice number already exists");
            }
            if (invoice.getIssueDate() == null) {
                invoice.setIssueDate(LocalDate.now());
            }
            return sanitize(invoiceDao.save(invoice));
        });
    }

    @Override
    public Invoice update(Invoice entity) throws RemoteException {
        return execute("update", () -> {
            Invoice invoice = normalize(entity, true);
            Invoice existing = invoiceDao.findInvoiceById(invoice.getInvoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
            invoiceDao.findByInvoiceNumber(invoice.getInvoiceNumber())
                    .filter(found -> !found.getInvoiceId().equals(invoice.getInvoiceId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Invoice number already exists");
                    });

            existing.setInvoiceNumber(invoice.getInvoiceNumber());
            existing.setTotalTaxAmount(invoice.getTotalTaxAmount());
            existing.setIssueDate(invoice.getIssueDate());
            return sanitize(invoiceDao.update(existing));
        });
    }

    @Override
    public Invoice findById(Long id) throws RemoteException {
        return execute("findById", () -> sanitize(invoiceDao.findInvoiceById(id).orElse(null)));
    }

    @Override
    public List<Invoice> findAll() throws RemoteException {
        return execute("findAll", () -> invoiceDao.findAllInvoices().stream().map(this::sanitize).toList());
    }

    private Invoice sanitize(Invoice invoice) {
        if (invoice != null) {
            Payment payment = invoice.getPayment();
            if (payment != null) {
                payment.setInvoice(null);
            }
        }
        return invoice;
    }

    private Invoice normalize(Invoice entity, boolean requireId) {
        if (entity == null) {
            throw new IllegalArgumentException("Invoice data is required");
        }
        if (requireId && entity.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }
        if (entity.getInvoiceNumber() == null || entity.getInvoiceNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice number is required");
        }
        if (entity.getTotalTaxAmount() == null
                || entity.getTotalTaxAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be greater than 0");
        }
        if (entity.getIssueDate() == null) {
            throw new IllegalArgumentException("Issue date is required");
        }
        entity.setInvoiceNumber(entity.getInvoiceNumber().trim());
        entity.setTotalTaxAmount(entity.getTotalTaxAmount().setScale(2, java.math.RoundingMode.HALF_UP));
        return entity;
    }
}
