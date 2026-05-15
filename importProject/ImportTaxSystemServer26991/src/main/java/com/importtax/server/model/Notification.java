package com.importtax.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "notifications")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "notification_type", nullable = false, length = 80)
    private String notificationType;

    @Column(name = "recipient", nullable = false, length = 150)
    private String recipient;

    @Column(name = "sent_at", nullable = false)
    private LocalDate sentAt;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    public Notification() {
    }

    public Notification(String message, String notificationType, String recipient, LocalDate sentAt, String status) {
        this.message = message;
        this.notificationType = notificationType;
        this.recipient = recipient;
        this.sentAt = sentAt;
        this.status = status;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public LocalDate getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDate sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Notification{"
                + "notificationId=" + notificationId
                + ", notificationType='" + notificationType + '\''
                + ", recipient='" + recipient + '\''
                + ", sentAt=" + sentAt
                + ", status='" + status + '\''
                + '}';
    }
}
