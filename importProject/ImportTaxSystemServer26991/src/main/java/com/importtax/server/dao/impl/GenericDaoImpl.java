package com.importtax.server.dao.impl;

import com.importtax.server.dao.DaoException;
import com.importtax.server.dao.GenericDao;
import com.importtax.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class GenericDaoImpl<T> implements GenericDao<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericDaoImpl.class);

    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;

    public GenericDaoImpl(Class<T> entityClass) {
        this(entityClass, HibernateUtil.getSessionFactory());
    }

    public GenericDaoImpl(Class<T> entityClass, SessionFactory sessionFactory) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory must not be null");
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return executeInTransaction(session -> {
            session.persist(entity);
            return entity;
        }, "save");
    }

    @Override
    public T update(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return executeInTransaction(session -> session.merge(entity), "update");
    }

    @Override
    public void delete(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        executeInTransaction(session -> {
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            return null;
        }, "delete");
    }

    @Override
    public Optional<T> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        return executeReadOnly(session -> Optional.ofNullable(session.find(entityClass, id)), "findById");
    }

    @Override
    public List<T> findAll() {
        return executeReadOnly(session -> session
                .createQuery("select e from " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList(), "findAll");
    }

    protected <R> R executeReadOnly(Function<Session, R> operation, String operationName) {
        try (Session session = sessionFactory.openSession()) {
            return operation.apply(session);
        } catch (RuntimeException exception) {
            LOGGER.error("DAO operation '{}' failed for entity '{}'.", operationName, entityClass.getSimpleName(), exception);
            throw new DaoException("Failed to " + operationName + " " + entityClass.getSimpleName(), exception);
        }
    }

    protected <R> R executeInTransaction(Function<Session, R> operation, String operationName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            R result = operation.apply(session);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            rollback(transaction, operationName);
            LOGGER.error("Transactional DAO operation '{}' failed for entity '{}'.",
                    operationName, entityClass.getSimpleName(), exception);
            throw new DaoException("Failed to " + operationName + " " + entityClass.getSimpleName(), exception);
        }
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private void rollback(Transaction transaction, String operationName) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (RuntimeException rollbackException) {
                LOGGER.error("Rollback failed for DAO operation '{}'.", operationName, rollbackException);
            }
        }
    }
}
