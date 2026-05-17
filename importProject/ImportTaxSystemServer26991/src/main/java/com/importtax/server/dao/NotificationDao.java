package com.importtax.server.dao;

import com.importtax.server.model.Notification;

import java.util.List;

public interface NotificationDao extends GenericDao<Notification> {

    List<Notification> findAllNotifications();
}
