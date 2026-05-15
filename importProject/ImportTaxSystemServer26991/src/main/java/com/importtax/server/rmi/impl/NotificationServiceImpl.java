package com.importtax.server.rmi.impl;

import com.importtax.server.dao.NotificationDao;
import com.importtax.server.dao.impl.NotificationDaoImpl;
import com.importtax.server.model.Notification;
import com.importtax.server.rmi.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class NotificationServiceImpl extends AbstractRemoteCrudService<Notification> implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    public NotificationServiceImpl() throws RemoteException {
        this(new NotificationDaoImpl());
    }

    public NotificationServiceImpl(NotificationDao notificationDao) throws RemoteException {
        super(notificationDao, LOGGER, "NotificationService");
    }
}
