package com.importtax.server.model;

import java.io.Serializable;

public class ImportStatusCounts implements Serializable {

    private static final long serialVersionUID = 1L;

    private long pending;
    private long paid;
    private long cleared;
    private long hold;

    public ImportStatusCounts() {
    }

    public ImportStatusCounts(long pending, long paid, long cleared, long hold) {
        this.pending = pending;
        this.paid = paid;
        this.cleared = cleared;
        this.hold = hold;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getPaid() {
        return paid;
    }

    public void setPaid(long paid) {
        this.paid = paid;
    }

    public long getCleared() {
        return cleared;
    }

    public void setCleared(long cleared) {
        this.cleared = cleared;
    }

    public long getHold() {
        return hold;
    }

    public void setHold(long hold) {
        this.hold = hold;
    }
}
