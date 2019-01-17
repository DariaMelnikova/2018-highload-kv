package ru.mail.polis.dariam;

import java.util.Base64;

import org.apache.commons.collections4.map.LRUMap;

public class UpdatesStorage {
    private Base64.Encoder encoder;
    private final LRUMap<String, Long> timestamps;

    public UpdatesStorage() {
        encoder = Base64.getUrlEncoder();
        timestamps = new LRUMap<>(1000, 0.75f);
    }

    public long getUpdateTime(byte[] key) {
        final Long time;
        synchronized (timestamps) {
            time = timestamps.get(getStringKey(key));
        }
        return time == null ? 0 : time;
    }

    public void updateTime(byte[] key) {
        synchronized (timestamps) {
            timestamps.put(getStringKey(key), System.currentTimeMillis());
        }
    }

    private String getStringKey(byte[] key) {
        return encoder.encodeToString(key);
    }
}
