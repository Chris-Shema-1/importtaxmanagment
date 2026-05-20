package com.importtax.server.rmi.impl;

import com.importtax.server.constants.ImportItemStatus;
import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.PaymentDao;
import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.ImportItemDaoImpl;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.dao.impl.PaymentDaoImpl;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.ImportStatusCounts;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.MonthlyTaxSummary;
import com.importtax.server.model.Payment;
import com.importtax.server.model.RecentActivityEntry;
import com.importtax.server.model.ReportSummary;
import com.importtax.server.model.ReportTableRow;
import com.importtax.server.rmi.ReportService;
import com.importtax.server.util.RemoteMessages;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportServiceImpl extends UnicastRemoteObject implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);
    private static final int RECENT_LIMIT = 15;
    private static final int REPORT_ROWS_LIMIT = 50;

    private final UserDao userDao;
    private final ImportItemDao importItemDao;
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;

    public ReportServiceImpl() throws RemoteException {
        this(new UserDaoImpl(), new ImportItemDaoImpl(), new InvoiceDaoImpl(), new PaymentDaoImpl());
    }

    public ReportServiceImpl(UserDao userDao, ImportItemDao importItemDao,
                             InvoiceDao invoiceDao, PaymentDao paymentDao) throws RemoteException {
        super();
        this.userDao = userDao;
        this.importItemDao = importItemDao;
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
    }

    @Override
    public ReportSummary getDashboardSummary() throws RemoteException {
        return execute(() -> {
            ReportSummary summary = new ReportSummary();
            summary.setTotalUsers(userDao.countUsers());
            summary.setTotalImports(importItemDao.countAllItems());
            summary.setPendingImports(importItemDao.countByStatus(ImportItemStatus.PENDING));
            summary.setPaidImports(importItemDao.countByStatus(ImportItemStatus.PAID));
            summary.setClearedImports(importItemDao.countByStatus(ImportItemStatus.CLEARED));
            summary.setHoldImports(importItemDao.countByStatus(ImportItemStatus.HOLD));
            summary.setTotalInvoices(invoiceDao.countInvoices());
            summary.setTotalPayments(paymentDao.countPayments());
            summary.setCompletedPayments(paymentDao.countByPaymentStatus("COMPLETED"));
            summary.setPendingPayments(paymentDao.countByPaymentStatus("PENDING"));
            summary.setTotalTaxCollected(invoiceDao.sumTotalTaxAmount());
            summary.setTotalRevenue(paymentDao.sumAmountByPaymentStatus("COMPLETED"));
            summary.setGeneratedAt(LocalDateTime.now());
            LOGGER.info("Dashboard summary generated: imports={}, invoices={}",
                    summary.getTotalImports(), summary.getTotalInvoices());
            return summary;
        });
    }

    @Override
    public ImportStatusCounts getImportStatusCounts() throws RemoteException {
        return execute(() -> new ImportStatusCounts(
                importItemDao.countByStatus(ImportItemStatus.PENDING),
                importItemDao.countByStatus(ImportItemStatus.PAID),
                importItemDao.countByStatus(ImportItemStatus.CLEARED),
                importItemDao.countByStatus(ImportItemStatus.HOLD)));
    }

    @Override
    public List<MonthlyTaxSummary> getMonthlyTaxSummary() throws RemoteException {
        return execute(() -> invoiceDao.aggregateTaxByMonth(6));
    }

    @Override
    public List<RecentActivityEntry> getRecentActivities() throws RemoteException {
        return execute(() -> {
            List<RecentActivityEntry> activities = new ArrayList<>();

            for (ImportItem item : importItemDao.findRecentItems(7)) {
                activities.add(new RecentActivityEntry(
                        "IMPORT",
                        "Import: " + safe(item.getItemName()),
                        item.getImportDate(),
                        safe(item.getStatus())));
            }
            for (Payment payment : paymentDao.findRecentPayments(7)) {
                initializePayment(payment);
                String inv = payment.getInvoice() != null
                        ? safe(payment.getInvoice().getInvoiceNumber()) : "—";
                activities.add(new RecentActivityEntry(
                        "PAYMENT",
                        "Payment " + formatMoney(payment.getAmountPaid()) + " for " + inv,
                        payment.getPaymentDate(),
                        safe(payment.getPaymentStatus())));
            }
            for (Invoice invoice : invoiceDao.findRecentInvoices(7)) {
                initializeInvoice(invoice);
                String itemName = invoice.getImportItem() != null
                        ? safe(invoice.getImportItem().getItemName()) : "—";
                activities.add(new RecentActivityEntry(
                        "INVOICE",
                        "Invoice " + safe(invoice.getInvoiceNumber()) + " — " + itemName,
                        invoice.getIssueDate(),
                        "ISSUED"));
            }

            activities.sort(Comparator
                    .comparing(RecentActivityEntry::getActivityDate,
                            Comparator.nullsLast(Comparator.reverseOrder())));
            if (activities.size() > RECENT_LIMIT) {
                return new ArrayList<>(activities.subList(0, RECENT_LIMIT));
            }
            return activities;
        });
    }

    @Override
    public List<ReportTableRow> getReportTableRows() throws RemoteException {
        return execute(() -> {
            List<ReportTableRow> rows = new ArrayList<>();
            for (Invoice invoice : invoiceDao.findRecentInvoices(REPORT_ROWS_LIMIT)) {
                initializeInvoice(invoice);
                ImportItem item = invoice.getImportItem();
                String userDisplay = "—";
                String itemName = "—";
                String status = "—";
                if (item != null) {
                    itemName = safe(item.getItemName());
                    status = safe(item.getStatus());
                    if (item.getUser() != null) {
                        Hibernate.initialize(item.getUser());
                        userDisplay = safe(item.getUser().getUsername());
                    } else {
                        userDisplay = safe(item.getImporterName());
                    }
                }
                rows.add(new ReportTableRow(
                        safe(invoice.getInvoiceNumber()),
                        itemName,
                        userDisplay,
                        invoice.getTotalTaxAmount(),
                        status,
                        invoice.getIssueDate()));
            }
            return rows;
        });
    }

    private void initializeInvoice(Invoice invoice) {
        if (invoice == null) {
            return;
        }
        if (invoice.getImportItem() != null) {
            Hibernate.initialize(invoice.getImportItem());
            if (invoice.getImportItem().getUser() != null) {
                Hibernate.initialize(invoice.getImportItem().getUser());
            }
        }
    }

    private void initializePayment(Payment payment) {
        if (payment != null && payment.getInvoice() != null) {
            Hibernate.initialize(payment.getInvoice());
        }
    }

    private static String safe(String value) {
        return value == null ? "—" : value;
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "$0";
        }
        return "$" + amount.toPlainString();
    }

    private <T> T execute(SupplierWithException<T> supplier) throws RemoteException {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("ReportService validation failed", e);
            throw new RemoteException(e.getMessage() != null ? e.getMessage() : "Invalid report request.");
        } catch (Exception e) {
            LOGGER.error("ReportService operation failed", e);
            throw RemoteMessages.toRemoteException("report", e);
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
