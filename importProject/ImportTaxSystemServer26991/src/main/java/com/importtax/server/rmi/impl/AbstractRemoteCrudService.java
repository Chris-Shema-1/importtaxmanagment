package com.importtax.server.rmi.impl;

import com.importtax.server.dao.GenericDao;
import com.importtax.server.rmi.RemoteCrudService;
import org.slf4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractRemoteCrudService<T> extends UnicastRemoteObject implements RemoteCrudService<T> {

    private final GenericDao<T> dao;
    private final Logger logger;
    private final String serviceName;

    protected AbstractRemoteCrudService(GenericDao<T> dao, Logger logger, String serviceName) throws RemoteException {
        super();
        this.dao = Objects.requireNonNull(dao, "dao must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
    }

    @Override
    public T save(T entity) throws RemoteException {
        return execute("save", () -> dao.save(entity));
    }

    @Override
    public T update(T entity) throws RemoteException {
        return execute("update", () -> dao.update(entity));
    }

    @Override
    public void delete(T entity) throws RemoteException {
        execute("delete", () -> {
            dao.delete(entity);
            return null;
        });
    }

    @Override
    public T findById(Long id) throws RemoteException {
        return execute("findById", () -> dao.findById(id).orElse(null));
    }

    @Override
    public List<T> findAll() throws RemoteException {
        return execute("findAll", dao::findAll);
    }

    protected <R> R execute(String operationName, Supplier<R> operation) throws RemoteException {
        try {
            logger.debug("{} operation '{}' started.", serviceName, operationName);
            R result = operation.get();
            logger.debug("{} operation '{}' completed.", serviceName, operationName);
            return result;
        } catch (RuntimeException exception) {
            logger.error("{} operation '{}' failed.", serviceName, operationName, exception);
            throw new RemoteException(serviceName + " operation '" + operationName + "' failed.", exception);
        }
    }
}
