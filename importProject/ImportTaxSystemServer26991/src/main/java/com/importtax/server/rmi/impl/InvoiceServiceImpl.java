package com.importtax.server.rmi.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.dao.impl.InvoiceDaoImpl;
import com.importtax.server.model.Invoice;
import com.importtax.server.rmi.InvoiceService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;

public class InvoiceServiceImpl extends AbstractRemoteCrudService<Invoice> implements InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceDao invoiceDao;

    public InvoiceServiceImpl() throws RemoteException {
        this(new InvoiceDaoImpl());
    }

    public InvoiceServiceImpl(InvoiceDao invoiceDao) throws RemoteException {
        super(invoiceDao, LOGGER, "InvoiceService");
        this.invoiceDao = invoiceDao;
    }

    @Override
    public Invoice findById(Long id) throws RemoteException {
        return execute("findById", () -> {
            Invoice invoice = invoiceDao.findById(id).orElse(null);
            if (invoice != null) {
                initializeLazyFields(invoice);
            }
            return invoice;
        });
    }

    @Override
    public List<Invoice> findAll() throws RemoteException {
        return execute("findAll", () -> {
            List<Invoice> invoices = invoiceDao.findAll();
            for (Invoice invoice : invoices) {
                initializeLazyFields(invoice);
            }
            return invoices;
        });
    }

    /**
     * Initialize lazy-loaded fields before RMI serialization
     */
    private void initializeLazyFields(Invoice invoice) {
        if (invoice == null) {
            return;
        }

        // Initialize the lazy-loaded importItem
        if (invoice.getImportItem() != null) {
            Hibernate.initialize(invoice.getImportItem());
        }

        // Initialize the lazy-loaded payment
        if (invoice.getPayment() != null) {
            Hibernate.initialize(invoice.getPayment());
        }
    }
}
