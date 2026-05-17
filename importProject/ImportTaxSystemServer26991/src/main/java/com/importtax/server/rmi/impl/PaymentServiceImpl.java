package com.importtax.server.rmi.impl;

import com.importtax.server.dao.PaymentDao;
import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.dao.impl.PaymentDaoImpl;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class PaymentServiceImpl extends AbstractRemoteCrudService<Payment> implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;

    public PaymentServiceImpl() throws RemoteException {
        this(new PaymentDaoImpl(), new InvoiceDaoImpl());
    }

    public PaymentServiceImpl(PaymentDao paymentDao) throws RemoteException {
        this(paymentDao, new InvoiceDaoImpl());
    }

    public PaymentServiceImpl(PaymentDao paymentDao, InvoiceDao invoiceDao) throws RemoteException {
        super(paymentDao, LOGGER, "PaymentService");
        this.paymentDao = paymentDao;
        this.invoiceDao = invoiceDao;
    }

    @Override
    public Payment save(Payment entity) throws RemoteException {
        return execute("save", () -> {
            Payment payment = normalize(entity, false);
            Invoice invoice = resolveInvoice(payment);
            if (paymentDao.findByInvoiceId(invoice.getInvoiceId()).isPresent()) {
                throw new IllegalArgumentException("A payment already exists for this invoice");
            }
            payment.setInvoice(invoice);
            if (payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDate.now());
            }
            return sanitize(paymentDao.save(payment));
        });
    }

    @Override
    public Payment update(Payment entity) throws RemoteException {
        return execute("update", () -> {
            Payment payment = normalize(entity, true);
            Payment existing = paymentDao.findPaymentById(payment.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            Invoice invoice = resolveInvoice(payment);
            paymentDao.findByInvoiceId(invoice.getInvoiceId())
                    .filter(found -> !found.getPaymentId().equals(payment.getPaymentId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("A payment already exists for this invoice");
                    });

            existing.setAmountPaid(payment.getAmountPaid());
            existing.setPaymentDate(payment.getPaymentDate());
            existing.setPaymentMethod(payment.getPaymentMethod());
            existing.setPaymentStatus(payment.getPaymentStatus());
            existing.setInvoice(invoice);
            return sanitize(paymentDao.update(existing));
        });
    }

    @Override
    public Payment findById(Long id) throws RemoteException {
        return execute("findById", () -> sanitize(paymentDao.findPaymentById(id).orElse(null)));
    }

    @Override
    public List<Payment> findAll() throws RemoteException {
        return execute("findAll", () -> paymentDao.findAllPayments().stream().map(this::sanitize).toList());
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) throws RemoteException {
        return execute("findByPaymentStatus", () -> paymentDao.findByPaymentStatus(normalizeStatus(paymentStatus))
                .stream().map(this::sanitize).toList());
    }

    private Payment normalize(Payment entity, boolean requireId) {
        if (entity == null) {
            throw new IllegalArgumentException("Payment data is required");
        }
        if (requireId && entity.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID is required");
        }
        if (entity.getAmountPaid() == null || entity.getAmountPaid().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount paid must be greater than 0");
        }
        if (entity.getInvoice() == null || entity.getInvoice().getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice is required");
        }
        if (entity.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment date is required");
        }
        if (entity.getPaymentMethod() == null || entity.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        entity.setAmountPaid(entity.getAmountPaid().setScale(2, java.math.RoundingMode.HALF_UP));
        entity.setPaymentMethod(entity.getPaymentMethod().trim());
        entity.setPaymentStatus(normalizeStatus(entity.getPaymentStatus()));
        return entity;
    }

    private Invoice resolveInvoice(Payment payment) {
        Invoice invoice = invoiceDao.findInvoiceById(payment.getInvoice().getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Selected invoice was not found"));
        if (payment.getAmountPaid().compareTo(invoice.getTotalTaxAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount must match the invoice amount exactly");
        }
        return invoice;
    }

    private String normalizeStatus(String value) {
        String status = value == null ? "" : value.trim().toUpperCase();
        if (!"PENDING".equals(status) && !"PAID".equals(status) && !"FAILED".equals(status)) {
            throw new IllegalArgumentException("Payment status must be PENDING, PAID, or FAILED");
        }
        return status;
    }

    private Payment sanitize(Payment payment) {
        if (payment != null && payment.getInvoice() != null) {
            payment.getInvoice().setPayment(null);
        }
        return payment;
    }
}
