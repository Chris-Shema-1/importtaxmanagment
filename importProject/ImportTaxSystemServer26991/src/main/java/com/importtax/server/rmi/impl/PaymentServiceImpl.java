package com.importtax.server.rmi.impl;

import com.importtax.server.constants.ImportItemStatus;
import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.PaymentDao;
import com.importtax.server.dao.impl.ImportItemDaoImpl;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.dao.impl.PaymentDaoImpl;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.PaymentService;
import com.importtax.server.service.NotificationWorkflowService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class PaymentServiceImpl extends AbstractRemoteCrudService<Payment> implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final String PAYMENT_COMPLETED = "COMPLETED";

    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;
    private final ImportItemDao importItemDao;
    private final NotificationWorkflowService notificationWorkflow;

    public PaymentServiceImpl() throws RemoteException {
        this(new PaymentDaoImpl(), new InvoiceDaoImpl(), new ImportItemDaoImpl(), new NotificationWorkflowService());
    }

    public PaymentServiceImpl(PaymentDao paymentDao, InvoiceDao invoiceDao, ImportItemDao importItemDao)
            throws RemoteException {
        this(paymentDao, invoiceDao, importItemDao, new NotificationWorkflowService());
    }

    public PaymentServiceImpl(PaymentDao paymentDao, InvoiceDao invoiceDao, ImportItemDao importItemDao,
                              NotificationWorkflowService notificationWorkflow) throws RemoteException {
        super(paymentDao, LOGGER, "PaymentService");
        this.paymentDao = paymentDao;
        this.invoiceDao = invoiceDao;
        this.importItemDao = importItemDao;
        this.notificationWorkflow = notificationWorkflow;
    }

    @Override
    public Payment save(Payment entity) throws RemoteException {
        return execute("save", () -> {
            Invoice invoice = validatePaymentBusinessRules(entity);
            entity.setInvoice(invoice);
            LOGGER.debug("Saving payment for invoice id={}", invoice.getInvoiceId());
            Payment saved = paymentDao.save(entity);
            LOGGER.info("Payment id={} persisted for invoice id={}, paymentStatus={}",
                    saved.getPaymentId(), invoice.getInvoiceId(), saved.getPaymentStatus());
            promoteImportItemIfCompleted(saved);
            Payment result = paymentDao.findById(saved.getPaymentId()).orElse(saved);
            initializeLazyFields(result);
            return result;
        });
    }

    @Override
    public Payment update(Payment entity) throws RemoteException {
        return execute("update", () -> {
            Invoice invoice = validatePaymentBusinessRules(entity);
            entity.setInvoice(invoice);
            LOGGER.debug("Updating payment id={}", entity.getPaymentId());
            Payment updated = paymentDao.update(entity);
            LOGGER.info("Payment id={} updated, paymentStatus={}", updated.getPaymentId(), updated.getPaymentStatus());
            promoteImportItemIfCompleted(updated);
            Payment result = paymentDao.findById(updated.getPaymentId()).orElse(updated);
            initializeLazyFields(result);
            return result;
        });
    }

    /**
     * Validates invoice existence, amount rules, and single-payment constraint.
     */
    private Invoice validatePaymentBusinessRules(Payment entity) {
        if (entity == null) {
            LOGGER.warn("Payment validation failed: entity is null");
            throw new IllegalArgumentException("Payment data is required");
        }
        if (entity.getInvoice() == null || entity.getInvoice().getInvoiceId() == null) {
            LOGGER.warn("Payment validation failed: invoice reference missing");
            throw new IllegalArgumentException("Invoice not found");
        }

        Long invoiceId = entity.getInvoice().getInvoiceId();
        Invoice invoice = invoiceDao.findById(invoiceId).orElse(null);
        if (invoice == null) {
            LOGGER.warn("Payment validation failed: invoice id={} does not exist", invoiceId);
            throw new IllegalArgumentException("Invoice not found. Create an invoice before recording payment.");
        }

        if (entity.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment date is required");
        }
        if (isBlank(entity.getPaymentMethod())) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (isBlank(entity.getPaymentStatus())) {
            throw new IllegalArgumentException("Payment status is required");
        }

        BigDecimal amount = entity.getAmountPaid();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            LOGGER.warn("Payment validation failed: non-positive amount for invoice id={}", invoiceId);
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        BigDecimal invoiceTotal = invoice.getTotalTaxAmount();
        if (invoiceTotal != null && invoiceTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invoice total cannot be negative");
        }
        if (invoiceTotal != null && amount.compareTo(invoiceTotal) > 0) {
            LOGGER.warn("Payment validation failed: amount {} exceeds invoice total {} for invoice id={}",
                    amount, invoiceTotal, invoiceId);
            throw new IllegalArgumentException("Payment amount cannot exceed invoice total.");
        }

        Optional<Payment> slot = paymentDao.findByInvoiceId(invoiceId);
        Long incomingId = entity.getPaymentId();

        if (slot.isPresent()) {
            Payment occupied = slot.get();
            boolean sameRow = incomingId != null && incomingId.equals(occupied.getPaymentId());
            if (!sameRow) {
                LOGGER.warn("Payment validation failed: invoice id={} already has payment id={}",
                        invoiceId, occupied.getPaymentId());
                if (PAYMENT_COMPLETED.equalsIgnoreCase(occupied.getPaymentStatus())) {
                    throw new IllegalArgumentException("This invoice has already been paid.");
                }
                throw new IllegalArgumentException("This invoice already has an associated payment.");
            }
        }

        return invoice;
    }

    private void promoteImportItemIfCompleted(Payment persisted) {
        if (persisted == null || persisted.getInvoice() == null || persisted.getInvoice().getInvoiceId() == null) {
            return;
        }
        if (!PAYMENT_COMPLETED.equalsIgnoreCase(persisted.getPaymentStatus())) {
            return;
        }

        Long invoiceId = persisted.getInvoice().getInvoiceId();
        Optional<Long> itemIdOpt = invoiceDao.findImportItemIdByInvoiceId(invoiceId);
        if (itemIdOpt.isEmpty()) {
            LOGGER.debug("No import item linked to invoice id={}, skipping PAID promotion", invoiceId);
            return;
        }

        importItemDao.findItemById(itemIdOpt.get()).ifPresent(item -> {
            String current = ImportItemStatus.canonicalFromDatabase(item.getStatus());
            if (ImportItemStatus.PAID.equals(current) || ImportItemStatus.CLEARED.equals(current)) {
                LOGGER.debug("Import item id={} already at {}, skipping promotion", item.getItemId(), current);
                return;
            }
            if (ImportItemStatus.PENDING.equals(current) || ImportItemStatus.HOLD.equals(current)) {
                item.setStatus(ImportItemStatus.PAID);
                importItemDao.updateItem(item);
                LOGGER.info("Import item id={} status {} -> PAID after payment id={}",
                        item.getItemId(), current, persisted.getPaymentId());
                importItemDao.findItemById(item.getItemId()).ifPresent(refreshed -> {
                    if (refreshed.getUser() != null) {
                        Hibernate.initialize(refreshed.getUser());
                    }
                    invoiceDao.findById(invoiceId).ifPresent(invoice ->
                            notificationWorkflow.handlePaymentSuccess(persisted, invoice, refreshed));
                });
            } else {
                LOGGER.warn("Import item id={} has unexpected status {}, not promoting to PAID",
                        item.getItemId(), current);
            }
        });
    }

    @Override
    public Payment findById(Long id) throws RemoteException {
        return execute("findById", () -> {
            Payment payment = paymentDao.findById(id).orElse(null);
            if (payment != null) {
                initializeLazyFields(payment);
            }
            return payment;
        });
    }

    @Override
    public List<Payment> findAll() throws RemoteException {
        return execute("findAll", () -> {
            List<Payment> payments = paymentDao.findAll();
            for (Payment payment : payments) {
                initializeLazyFields(payment);
            }
            return payments;
        });
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) throws RemoteException {
        return execute("findByPaymentStatus", () -> {
            List<Payment> payments = paymentDao.findByPaymentStatus(paymentStatus);
            for (Payment payment : payments) {
                initializeLazyFields(payment);
            }
            return payments;
        });
    }

    private void validateCallerRole(Long callerUserId) throws RemoteException {
        if (callerUserId == null) {
            return;
        }
        com.importtax.server.dao.UserDao userDao = new com.importtax.server.dao.impl.UserDaoImpl();
        com.importtax.server.model.User caller = userDao.findById(callerUserId).orElse(null);
        if (caller == null) {
            throw new IllegalArgumentException("Caller user not found.");
        }
        String role = caller.getRole();
        if ("CUSTOMS_OFFICER".equalsIgnoreCase(role)) {
            throw new SecurityException("Access denied. Customs Officer cannot manage payments.");
        }
    }

    @Override
    public Payment savePaymentSecure(Payment payment, Long callerUserId) throws RemoteException {
        validateCallerRole(callerUserId);
        return save(payment);
    }

    @Override
    public Payment updatePaymentSecure(Payment payment, Long callerUserId) throws RemoteException {
        validateCallerRole(callerUserId);
        return update(payment);
    }

    @Override
    public void deletePaymentSecure(Long paymentId, Long callerUserId) throws RemoteException {
        validateCallerRole(callerUserId);
        execute("deletePaymentSecure", () -> {
            if (paymentId == null) {
                throw new IllegalArgumentException("Payment ID is required");
            }
            Payment payment = paymentDao.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            paymentDao.delete(payment);
            return null;
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Prepare payment graphs for RMI without touching lazy associations outside an open session.
     */
    private void initializeLazyFields(Payment payment) {
        if (payment == null) {
            return;
        }
        Invoice invoice = payment.getInvoice();
        if (invoice != null) {
            invoice.setImportItem(null);
            invoice.setPayment(null);
        }
    }
}
