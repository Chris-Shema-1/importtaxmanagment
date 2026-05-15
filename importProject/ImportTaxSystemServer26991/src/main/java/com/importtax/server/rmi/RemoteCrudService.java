package com.importtax.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteCrudService<T> extends Remote {

    T save(T entity) throws RemoteException;

    T update(T entity) throws RemoteException;

    void delete(T entity) throws RemoteException;

    T findById(Long id) throws RemoteException;

    List<T> findAll() throws RemoteException;
}
