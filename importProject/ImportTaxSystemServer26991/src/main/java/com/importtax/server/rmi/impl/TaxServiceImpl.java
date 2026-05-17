package com.importtax.server.rmi.impl;

import com.importtax.server.dao.TaxDao;
import com.importtax.server.dao.impl.TaxDaoImpl;
import com.importtax.server.model.Tax;
import com.importtax.server.rmi.TaxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;

public class TaxServiceImpl extends AbstractRemoteCrudService<Tax> implements TaxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxServiceImpl.class);
    private final TaxDao taxDao;

    public TaxServiceImpl() throws RemoteException {
        this(new TaxDaoImpl());
    }

    public TaxServiceImpl(TaxDao taxDao) throws RemoteException {
        super(taxDao, LOGGER, "TaxService");
        this.taxDao = taxDao;
    }

    @Override
    public Tax save(Tax entity) throws RemoteException {
        return execute("save", () -> {
            Tax tax = normalize(entity, false);
            if (taxDao.findByTaxName(tax.getTaxName()).isPresent()) {
                throw new IllegalArgumentException("Tax name already exists");
            }
            return taxDao.save(tax);
        });
    }

    @Override
    public Tax update(Tax entity) throws RemoteException {
        return execute("update", () -> {
            Tax tax = normalize(entity, true);
            Tax existing = taxDao.findById(tax.getTaxId())
                    .orElseThrow(() -> new IllegalArgumentException("Tax record not found"));
            taxDao.findByTaxName(tax.getTaxName())
                    .filter(found -> !found.getTaxId().equals(tax.getTaxId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Tax name already exists");
                    });

            existing.setTaxName(tax.getTaxName());
            existing.setTaxRate(tax.getTaxRate());
            existing.setDescription(tax.getDescription());
            return taxDao.update(existing);
        });
    }

    @Override
    public List<Tax> findAll() throws RemoteException {
        return execute("findAll", super::findAll);
    }

    private Tax normalize(Tax entity, boolean requireId) {
        if (entity == null) {
            throw new IllegalArgumentException("Tax data is required");
        }
        if (requireId && entity.getTaxId() == null) {
            throw new IllegalArgumentException("Tax ID is required");
        }
        if (entity.getTaxName() == null || entity.getTaxName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tax name is required");
        }
        if (entity.getTaxRate() == null || entity.getTaxRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tax rate must be greater than 0");
        }
        entity.setTaxName(entity.getTaxName().trim());
        entity.setTaxRate(entity.getTaxRate().setScale(4, java.math.RoundingMode.HALF_UP));
        entity.setDescription(entity.getDescription() == null || entity.getDescription().trim().isEmpty()
                ? null : entity.getDescription().trim());
        return entity;
    }
}
