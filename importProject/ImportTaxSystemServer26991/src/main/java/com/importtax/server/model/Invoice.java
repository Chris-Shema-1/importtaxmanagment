package com.importtax.server.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 80)
    private String invoiceNumber;

    @Column(name = "total_tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTaxAmount;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Payment payment;

    public Invoice() {
    }

    public Invoice(String invoiceNumber, BigDecimal totalTaxAmount, LocalDate issueDate) {
        this.invoiceNumber = invoiceNumber;
        this.totalTaxAmount = totalTaxAmount;
        this.issueDate = issueDate;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalTaxAmount() {
        return totalTaxAmount;
    }

    public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
        this.totalTaxAmount = totalTaxAmount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setInvoice(this);
        }
    }

    @Override
    public String toString() {
        Long paymentId = payment != null ? payment.getPaymentId() : null;
        return "Invoice{"
                + "invoiceId=" + invoiceId
                + ", invoiceNumber='" + invoiceNumber + '\''
                + ", totalTaxAmount=" + totalTaxAmount
                + ", issueDate=" + issueDate
                + ", paymentId=" + paymentId
                + '}';
    }
}
