package com.importtax.server.dao.impl;

import com.importtax.server.dao.NotificationDao;
import com.importtax.server.model.Notification;
import org.hibernate.SessionFactory;

public class NotificationDaoImpl extends GenericDaoImpl<Notification> implements NotificationDao {

    public NotificationDaoImpl() {
        super(Notification.class);
    }

    public NotificationDaoImpl(SessionFactory sessionFactory) {
        super(Notification.class, sessionFactory);
    }
}
