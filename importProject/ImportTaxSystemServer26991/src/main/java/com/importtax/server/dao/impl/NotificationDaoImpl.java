package com.importtax.server.dao.impl;

import com.importtax.server.dao.NotificationDao;
import com.importtax.server.model.Notification;
import org.hibernate.SessionFactory;

import java.util.List;

public class NotificationDaoImpl extends GenericDaoImpl<Notification> implements NotificationDao {

    public NotificationDaoImpl() {
        super(Notification.class);
    }

    public NotificationDaoImpl(SessionFactory sessionFactory) {
        super(Notification.class, sessionFactory);
    }

    @Override
    public List<Notification> findAllNotifications() {
        return executeReadOnly(session -> session.createQuery(
                "select n from Notification n order by n.sentAt desc, n.notificationId desc",
                Notification.class).getResultList(), "findAllNotifications");
    }
}
