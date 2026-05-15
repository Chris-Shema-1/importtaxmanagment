package com.importtax.server.dao.impl;

public abstract class AbstractHibernateDao<T> extends GenericDaoImpl<T> {

    protected AbstractHibernateDao(Class<T> entityClass) {
        super(entityClass);
    }
}
