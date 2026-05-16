package com.importtax.server.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal totalTaxAmount;
    private LocalDate issueDate;
    private Payment payment;

    public Invoice() {}

    public Invoice(String invoiceNumber, BigDecimal totalTaxAmount, LocalDate issueDate) {
        this.invoiceNumber = invoiceNumber;
        this.totalTaxAmount = totalTaxAmount;
        this.issueDate = issueDate;
    }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}
