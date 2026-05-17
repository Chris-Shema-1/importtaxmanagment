package com.importtax.server.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Tax implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taxId;
    private String taxName;
    private BigDecimal taxRate;
    private String description;

    public Long getTaxId() {
        return taxId;
    }

    public void setTaxId(Long taxId) {
        this.taxId = taxId;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
