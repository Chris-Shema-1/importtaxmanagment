package com.importtax.server.rmi.impl;

import com.importtax.server.rmi.ImportTaxRemoteService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ImportTaxRemoteServiceImpl extends UnicastRemoteObject implements ImportTaxRemoteService {

    public ImportTaxRemoteServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String ping() throws RemoteException {
        return "Import Tax System Server is running.";
    }
}
