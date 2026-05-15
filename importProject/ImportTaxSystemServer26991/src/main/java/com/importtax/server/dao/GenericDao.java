package com.importtax.server.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {

    T save(T entity);

    T update(T entity);

    void delete(T entity);

    Optional<T> findById(Long id);

    List<T> findAll();
}
