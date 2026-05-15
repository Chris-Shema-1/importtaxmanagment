package com.importtax.server.rmi.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.model.Invoice;
import com.importtax.server.rmi.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class InvoiceServiceImpl extends AbstractRemoteCrudService<Invoice> implements InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    public InvoiceServiceImpl() throws RemoteException {
        this(new InvoiceDaoImpl());
    }

    public InvoiceServiceImpl(InvoiceDao invoiceDao) throws RemoteException {
        super(invoiceDao, LOGGER, "InvoiceService");
    }
}
