package dev.khaliuk.ccredis.storage;

import dev.khaliuk.ccredis.protocol.ValueType;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private static final Map<String, StorageRecord> cache = new ConcurrentHashMap<>();

    private Storage() {
    }

    public static void put(String key, Object value) {
        cache.put(key, new StorageRecord(ValueType.STRING, value, Instant.MAX));
    }

    public static void put(String key, String value, Long expiration) {
        cache.put(key, new StorageRecord(ValueType.STRING, value, Instant.now().plusMillis(expiration)));
    }

    public static void put(String key, ValueType valueType, Object value) {
        cache.put(key, new StorageRecord(valueType, value, Instant.MAX));
    }

    public static StorageRecord get(String key) {
        StorageRecord storageRecord = cache.get(key);
        if (storageRecord == null) {
            return null;
        }
        if (Instant.now().isAfter(storageRecord.expiration())) {
            remove(key);
            return null;
        }
        return storageRecord;
    }

    public static void remove(String key) {
        cache.remove(key);
    }

    public static boolean contains(String key) {
        return cache.containsKey(key);
    }
}
