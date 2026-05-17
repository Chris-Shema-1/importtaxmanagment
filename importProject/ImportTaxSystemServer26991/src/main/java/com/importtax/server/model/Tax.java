package com.importtax.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taxes")
public class Tax implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_id")
    private Long taxId;

    @Column(name = "tax_name", nullable = false, unique = true, length = 120)
    private String taxName;

    @Column(name = "tax_rate", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToMany(mappedBy = "appliedTaxes")
    private List<ImportItem> importItems = new ArrayList<>();

    public Tax() {
    }

    public Tax(String taxName, BigDecimal taxRate, String description) {
        this.taxName = taxName;
        this.taxRate = taxRate;
        this.description = description;
    }

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

    @Override
    public String toString() {
        return "Tax{"
                + "taxId=" + taxId
                + ", taxName='" + taxName + '\''
                + ", taxRate=" + taxRate
                + ", description='" + description + '\''
                + '}';
    }
}
