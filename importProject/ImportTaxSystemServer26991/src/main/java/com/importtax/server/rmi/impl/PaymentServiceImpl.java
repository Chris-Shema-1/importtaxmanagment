package com.importtax.server.rmi.impl;

import com.importtax.server.dao.PaymentDao;
import com.importtax.server.dao.impl.PaymentDaoImpl;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;

public class PaymentServiceImpl extends AbstractRemoteCrudService<Payment> implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentDao paymentDao;

    public PaymentServiceImpl() throws RemoteException {
        this(new PaymentDaoImpl());
    }

    public PaymentServiceImpl(PaymentDao paymentDao) throws RemoteException {
        super(paymentDao, LOGGER, "PaymentService");
        this.paymentDao = paymentDao;
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) throws RemoteException {
        return execute("findByPaymentStatus", () -> paymentDao.findByPaymentStatus(paymentStatus));
    }
}
