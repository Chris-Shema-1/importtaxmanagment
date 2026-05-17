package com.importtax.server.rmi.impl;

import com.importtax.server.dao.NotificationDao;
import com.importtax.server.dao.impl.NotificationDaoImpl;
import com.importtax.server.model.Notification;
import com.importtax.server.rmi.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class NotificationServiceImpl extends AbstractRemoteCrudService<Notification> implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationDao notificationDao;

    public NotificationServiceImpl() throws RemoteException {
        this(new NotificationDaoImpl());
    }

    public NotificationServiceImpl(NotificationDao notificationDao) throws RemoteException {
        super(notificationDao, LOGGER, "NotificationService");
        this.notificationDao = notificationDao;
    }

    @Override
    public Notification save(Notification entity) throws RemoteException {
        return execute("save", () -> notificationDao.save(normalize(entity, false)));
    }

    @Override
    public Notification update(Notification entity) throws RemoteException {
        return execute("update", () -> {
            Notification input = normalize(entity, true);
            Notification existing = notificationDao.findById(input.getNotificationId())
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
            existing.setMessage(input.getMessage());
            existing.setNotificationType(input.getNotificationType());
            existing.setRecipient(input.getRecipient());
            existing.setSentAt(input.getSentAt());
            existing.setStatus(input.getStatus());
            return notificationDao.update(existing);
        });
    }

    @Override
    public List<Notification> findAll() throws RemoteException {
        return execute("findAll", notificationDao::findAllNotifications);
    }

    private Notification normalize(Notification entity, boolean requireId) {
        if (entity == null) {
            throw new IllegalArgumentException("Notification data is required");
        }
        if (requireId && entity.getNotificationId() == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (entity.getMessage() == null || entity.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (entity.getNotificationType() == null || entity.getNotificationType().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (entity.getRecipient() == null || entity.getRecipient().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (entity.getSentAt() == null) {
            entity.setSentAt(LocalDate.now());
        }
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification status is required");
        }
        entity.setMessage(entity.getMessage().trim());
        entity.setNotificationType(entity.getNotificationType().trim().toUpperCase());
        entity.setRecipient(entity.getRecipient().trim());
        entity.setStatus(entity.getStatus().trim().toUpperCase());
        return entity;
    }
}
