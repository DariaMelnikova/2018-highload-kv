package ru.mail.polis.dariam.httpclient;

public class HttpQueryResult {
    private final int status;
    private final byte[] data;
    private final boolean deleted;
    private final Long timestamp;

    public HttpQueryResult(int status, byte[] data, boolean deleted, Long timestamp) {
        this.status = status;
        this.data = data;
        this.deleted = deleted;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
