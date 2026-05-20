package com.importtax.server.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregated dashboard statistics (server-side only).
 */
public class ReportSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalUsers;
    private long totalImports;
    private long pendingImports;
    private long paidImports;
    private long clearedImports;
    private long holdImports;
    private long totalInvoices;
    private long totalPayments;
    private long completedPayments;
    private long pendingPayments;
    private BigDecimal totalTaxCollected;
    private BigDecimal totalRevenue;
    private LocalDateTime generatedAt;

    public ReportSummary() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalImports() {
        return totalImports;
    }

    public void setTotalImports(long totalImports) {
        this.totalImports = totalImports;
    }

    public long getPendingImports() {
        return pendingImports;
    }

    public void setPendingImports(long pendingImports) {
        this.pendingImports = pendingImports;
    }

    public long getPaidImports() {
        return paidImports;
    }

    public void setPaidImports(long paidImports) {
        this.paidImports = paidImports;
    }

    public long getClearedImports() {
        return clearedImports;
    }

    public void setClearedImports(long clearedImports) {
        this.clearedImports = clearedImports;
    }

    public long getHoldImports() {
        return holdImports;
    }

    public void setHoldImports(long holdImports) {
        this.holdImports = holdImports;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getCompletedPayments() {
        return completedPayments;
    }

    public void setCompletedPayments(long completedPayments) {
        this.completedPayments = completedPayments;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public BigDecimal getTotalTaxCollected() {
        return totalTaxCollected;
    }

    public void setTotalTaxCollected(BigDecimal totalTaxCollected) {
        this.totalTaxCollected = totalTaxCollected;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
