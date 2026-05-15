package com.importtax.server.rmi;

import com.importtax.server.model.Payment;

import java.rmi.RemoteException;
import java.util.List;

public interface PaymentService extends RemoteCrudService<Payment> {

    List<Payment> findByPaymentStatus(String paymentStatus) throws RemoteException;
}
