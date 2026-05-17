package com.importtax.server.dao;

import com.importtax.server.model.Tax;

import java.util.Optional;

public interface TaxDao extends GenericDao<Tax> {

    Optional<Tax> findByTaxName(String taxName);
}
