package com.importtax.server.model;

import java.io.Serializable;
import java.time.LocalDate;

public class RecentActivityEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityType;
    private String description;
    private LocalDate activityDate;
    private String status;

    public RecentActivityEntry() {
    }

    public RecentActivityEntry(String activityType, String description, LocalDate activityDate, String status) {
        this.activityType = activityType;
        this.description = description;
        this.activityDate = activityDate;
        this.status = status;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
