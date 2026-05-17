package com.importtax.server.dao;

import com.importtax.server.model.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceDao extends GenericDao<Invoice> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAllInvoices();

    Optional<Invoice> findInvoiceById(Long invoiceId);
}
