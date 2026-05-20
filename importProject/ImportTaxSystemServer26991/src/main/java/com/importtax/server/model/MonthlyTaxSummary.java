package com.importtax.server.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class MonthlyTaxSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private String month;
    private BigDecimal totalTax;

    public MonthlyTaxSummary() {
    }

    public MonthlyTaxSummary(String month, BigDecimal totalTax) {
        this.month = month;
        this.totalTax = totalTax;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }
}
