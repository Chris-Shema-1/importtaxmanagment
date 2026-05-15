package com.importtax.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ImportTaxRemoteService extends Remote {

    String ping() throws RemoteException;
}
