package com.importtax.server.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Flat row for the reports detail table (invoice-centric view).
 */
public class ReportTableRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceNumber;
    private String importItemName;
    private String userDisplay;
    private BigDecimal amount;
    private String status;
    private LocalDate recordDate;

    public ReportTableRow() {
    }

    public ReportTableRow(String invoiceNumber, String importItemName, String userDisplay,
                          BigDecimal amount, String status, LocalDate recordDate) {
        this.invoiceNumber = invoiceNumber;
        this.importItemName = importItemName;
        this.userDisplay = userDisplay;
        this.amount = amount;
        this.status = status;
        this.recordDate = recordDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getImportItemName() {
        return importItemName;
    }

    public void setImportItemName(String importItemName) {
        this.importItemName = importItemName;
    }

    public String getUserDisplay() {
        return userDisplay;
    }

    public void setUserDisplay(String userDisplay) {
        this.userDisplay = userDisplay;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
}
