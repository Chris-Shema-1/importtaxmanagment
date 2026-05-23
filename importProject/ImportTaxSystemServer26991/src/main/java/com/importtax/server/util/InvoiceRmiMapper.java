package com.importtax.server.util;

import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;

import java.util.ArrayList;

/**
 * Builds plain Serializable invoice graphs for RMI (no Hibernate proxies or cycles).
 */
public final class InvoiceRmiMapper {

    private InvoiceRmiMapper() {
    }

    /**
     * Copies scalar fields only. Call while the Hibernate session is still open.
     */
    public static Invoice toSafeInvoice(Invoice managed) {
        if (managed == null) {
            return null;
        }
        Invoice safe = new Invoice();
        safe.setInvoiceId(managed.getInvoiceId());
        safe.setInvoiceNumber(managed.getInvoiceNumber());
        safe.setTotalTaxAmount(managed.getTotalTaxAmount());
        safe.setIssueDate(managed.getIssueDate());

        ImportItem srcItem = managed.getImportItem();
        if (srcItem != null) {
            safe.setImportItem(toSafeImportItem(srcItem));
        }
        safe.setPayment(null);
        return safe;
    }

    /**
     * Copies import-item scalars only; never touches lazy user or tax collections.
     */
    public static ImportItem toSafeImportItem(ImportItem src) {
        return ImportItemRmiMapper.toSafeImportItem(src);
    }
}
