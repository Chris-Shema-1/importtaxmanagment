package com.importtax.server.service;

import com.importtax.server.broker.NotificationBroker;
import com.importtax.server.constants.NotificationType;
import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.NotificationDao;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.dao.impl.NotificationDaoImpl;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Notification;
import com.importtax.server.model.Payment;
import com.importtax.server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persists workflow notifications and publishes business events to ActiveMQ.
 */
public class NotificationWorkflowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationWorkflowService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final NotificationDao notificationDao;
    private final InvoiceDao invoiceDao;

    public NotificationWorkflowService() {
        this(new NotificationDaoImpl(), new InvoiceDaoImpl());
    }

    public NotificationWorkflowService(NotificationDao notificationDao, InvoiceDao invoiceDao) {
        this.notificationDao = notificationDao;
        this.invoiceDao = invoiceDao;
    }

    public void handlePaymentSuccess(Payment payment, Invoice invoice, ImportItem item) {
        if (payment == null || invoice == null || item == null) {
            return;
        }
        String invoiceNumber = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-" + invoice.getInvoiceId();
        String itemName = item.getItemName();
        BigDecimal amount = payment.getAmountPaid();
        LocalDate paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();
        User owner = item.getUser();
        String username = resolveUsername(owner);
        String email = resolveEmail(owner);

        String message = buildPaymentMessage(invoiceNumber, itemName, amount, paymentDate);
        String title = "Payment Confirmed";

        try {
            Notification stored = persistNotification(message, NotificationType.PAYMENT_CONFIRMED, username);
            LOGGER.info("Payment notification stored id={} for recipient={}", stored.getNotificationId(), username);
        } catch (Exception e) {
            LOGGER.error("Failed to store payment notification for invoice {}", invoiceNumber, e);
        }

        try {
            NotificationBroker.publishPaymentNotification(
                    invoiceNumber,
                    itemName,
                    amount,
                    username,
                    email,
                    LocalDateTime.now(),
                    NotificationType.PAYMENT_CONFIRMED,
                    title,
                    message);
        } catch (Exception e) {
            LOGGER.error("Failed to publish payment notification for invoice {}", invoiceNumber, e);
        }
    }

    public void handleClearance(ImportItem item) {
        if (item == null) {
            return;
        }
        String itemName = item.getItemName();
        String importerName = item.getImporterName();
        LocalDate clearanceDate = LocalDate.now();
        String invoiceRef = invoiceDao.findInvoiceNumberByImportItemId(item.getItemId()).orElse("N/A");
        User owner = item.getUser();
        String username = resolveUsername(owner);
        String email = resolveEmail(owner);

        String message = buildClearanceMessage(itemName, importerName, clearanceDate, invoiceRef);
        String title = "Import Cleared";

        try {
            Notification stored = persistNotification(message, NotificationType.IMPORT_CLEARED, username);
            LOGGER.info("Clearance notification stored id={} for recipient={}", stored.getNotificationId(), username);
        } catch (Exception e) {
            LOGGER.error("Failed to store clearance notification for item id={}", item.getItemId(), e);
        }

        try {
            NotificationBroker.publishClearanceNotification(
                    itemName,
                    importerName,
                    clearanceDate,
                    invoiceRef,
                    username,
                    email,
                    LocalDateTime.now(),
                    NotificationType.IMPORT_CLEARED,
                    title,
                    message);
        } catch (Exception e) {
            LOGGER.error("Failed to publish clearance notification for item id={}", item.getItemId(), e);
        }
    }

    private Notification persistNotification(String message, String type, String recipient) {
        Notification notification = new Notification(
                message,
                type,
                recipient,
                LocalDate.now(),
                NotificationType.STATUS_UNREAD);
        return notificationDao.save(notification);
    }

    private static String buildPaymentMessage(String invoiceNumber, String itemName,
                                              BigDecimal amount, LocalDate paymentDate) {
        String amountStr = amount != null ? amount.toPlainString() : "0";
        String dateStr = paymentDate != null ? paymentDate.format(DATE_FMT) : LocalDate.now().format(DATE_FMT);
        return "Payment received successfully for Invoice #" + invoiceNumber + ".\n"
                + "Your import item payment has been confirmed.\n\n"
                + "Amount paid: " + amountStr + "\n"
                + "Item: " + (itemName != null ? itemName : "—") + "\n"
                + "Payment date: " + dateStr;
    }

    private static String buildClearanceMessage(String itemName, String importerName,
                                                LocalDate clearanceDate, String invoiceRef) {
        String dateStr = clearanceDate.format(DATE_FMT);
        return "Your import item has been cleared successfully.\n\n"
                + "Item: " + (itemName != null ? itemName : "—") + "\n"
                + "Importer: " + (importerName != null ? importerName : "—") + "\n"
                + "Clearance date: " + dateStr + "\n"
                + "Invoice reference: " + invoiceRef;
    }

    private static String resolveUsername(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return "system";
        }
        return user.getUsername().trim();
    }

    private static String resolveEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return "";
        }
        return user.getEmail().trim();
    }
}
