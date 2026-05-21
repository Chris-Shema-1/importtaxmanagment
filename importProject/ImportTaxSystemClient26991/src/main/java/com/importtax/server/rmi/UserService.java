package com.importtax.server.rmi;

import com.importtax.server.model.User;
import java.rmi.RemoteException;

public interface UserService extends RemoteCrudService<User> {

    User login(String username, String password) throws RemoteException;

    User registerUser(User user) throws RemoteException;

    User authenticateUser(String username, String password) throws RemoteException;

    boolean isUsernameUnique(String username) throws RemoteException;

    boolean isEmailUnique(String email) throws RemoteException;

    User getUserById(Long userId) throws RemoteException;

    User findByUsername(String username) throws RemoteException;

    User updateUserSecure(User user, Long callerUserId) throws RemoteException;

    void deleteUserSecure(Long userIdToDelete, Long callerUserId) throws RemoteException;
}
