package com.importtax.server.dao.impl;

import com.importtax.server.dao.UserDao;
import com.importtax.server.model.User;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends GenericDaoImpl<User> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    @Override
    public Optional<User> login(String username, String password) {
        return executeReadOnly(session -> {
            TypedQuery<User> query = session.createQuery(
                    "select u from User u where u.username = :username and u.password = :password",
                    User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return query.getResultStream().findFirst();
        }, "login");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return executeReadOnly(session -> {
            TypedQuery<User> query = session.createQuery(
                    "select u from User u where u.username = :username",
                    User.class);
            query.setParameter("username", username);
            return query.getResultStream().findFirst();
        }, "findByUsername");
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return executeReadOnly(session -> {
            TypedQuery<User> query = session.createQuery(
                    "select u from User u where lower(u.email) = :email",
                    User.class);
            query.setParameter("email", email.toLowerCase());
            return query.getResultStream().findFirst();
        }, "findByEmail");
    }

    @Override
    public List<User> findAllUsers() {
        return executeReadOnly(session -> session.createQuery(
                "select u from User u order by u.createdAt desc, u.userId desc",
                User.class).getResultList(), "findAllUsers");
    }
}
