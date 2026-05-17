package com.importtax.server.dao.impl;

import com.importtax.server.dao.TaxDao;
import com.importtax.server.model.Tax;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.Optional;

public class TaxDaoImpl extends GenericDaoImpl<Tax> implements TaxDao {

    public TaxDaoImpl() {
        super(Tax.class);
    }

    public TaxDaoImpl(SessionFactory sessionFactory) {
        super(Tax.class, sessionFactory);
    }

    @Override
    public Optional<Tax> findByTaxName(String taxName) {
        return executeReadOnly(session -> {
            TypedQuery<Tax> query = session.createQuery(
                    "select t from Tax t where lower(t.taxName) = :taxName",
                    Tax.class);
            query.setParameter("taxName", taxName.toLowerCase());
            return query.getResultStream().findFirst();
        }, "findByTaxName");
    }
}
