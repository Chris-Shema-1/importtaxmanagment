package com.importtax.server.rmi.impl;

import com.importtax.server.dao.TaxDao;
import com.importtax.server.dao.impl.TaxDaoImpl;
import com.importtax.server.model.Tax;
import com.importtax.server.rmi.TaxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class TaxServiceImpl extends AbstractRemoteCrudService<Tax> implements TaxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxServiceImpl.class);

    public TaxServiceImpl() throws RemoteException {
        this(new TaxDaoImpl());
    }

    public TaxServiceImpl(TaxDao taxDao) throws RemoteException {
        super(taxDao, LOGGER, "TaxService");
    }
}
