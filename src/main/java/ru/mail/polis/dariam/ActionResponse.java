package ru.mail.polis.dariam;

public class ActionResponse {
    private Long timestamp;

    private byte[] response;
    private int status;
    private boolean deleted = false;

    public ActionResponse withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ActionResponse withDeleted() {
        deleted = true;
        return this;
    }

    public ActionResponse(int status) {
        this.status = status;
    }

    public ActionResponse(int status, byte[] response) {
        this.status = status;
        this.response = response;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public byte[] getResponse() {
        return response;
    }

    public int getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
