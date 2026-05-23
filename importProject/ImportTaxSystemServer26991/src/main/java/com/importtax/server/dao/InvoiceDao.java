package com.importtax.server.dao;

import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.MonthlyTaxSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceDao extends GenericDao<Invoice> {

    /** Item linked to this invoice, if any (by FK). */
    Optional<Long> findImportItemIdByInvoiceId(Long invoiceId);

    /** Invoice number for an import item, if an invoice exists. */
    Optional<String> findInvoiceNumberByImportItemId(Long itemId);

    /**
     * Creates and persists an invoice linked to the import item when none exists yet.
     *
     * @return the new invoice, or empty if an invoice already exists for the item
     */
    Optional<Invoice> createInvoiceForImportItemIfAbsent(ImportItem importItem);

    long countInvoices();

    BigDecimal sumTotalTaxAmount();

    List<MonthlyTaxSummary> aggregateTaxByMonth(int maxMonths);

    List<Invoice> findRecentInvoices(int limit);
}
