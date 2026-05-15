package com.importtax.server.dao.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.model.Invoice;
import org.hibernate.SessionFactory;

public class InvoiceDaoImpl extends GenericDaoImpl<Invoice> implements InvoiceDao {

    public InvoiceDaoImpl() {
        super(Invoice.class);
    }

    public InvoiceDaoImpl(SessionFactory sessionFactory) {
        super(Invoice.class, sessionFactory);
    }
}
