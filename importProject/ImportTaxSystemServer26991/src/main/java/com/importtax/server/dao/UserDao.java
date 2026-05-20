package com.importtax.server.dao;

import com.importtax.server.model.User;

import java.util.Optional;

public interface UserDao extends GenericDao<User> {

    Optional<User> login(String username, String password);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /** Resolve account by username or email address (case-insensitive email). */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    long countUsers();
}
