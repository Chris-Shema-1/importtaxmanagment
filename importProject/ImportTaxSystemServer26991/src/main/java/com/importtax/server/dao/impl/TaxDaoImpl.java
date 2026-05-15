package com.importtax.server.dao.impl;

import com.importtax.server.dao.TaxDao;
import com.importtax.server.model.Tax;
import org.hibernate.SessionFactory;

public class TaxDaoImpl extends GenericDaoImpl<Tax> implements TaxDao {

    public TaxDaoImpl() {
        super(Tax.class);
    }

    public TaxDaoImpl(SessionFactory sessionFactory) {
        super(Tax.class, sessionFactory);
    }
}
