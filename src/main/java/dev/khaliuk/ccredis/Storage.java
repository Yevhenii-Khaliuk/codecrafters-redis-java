package dev.khaliuk.ccredis;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private static final Map<String, String> storage = new HashMap<>();
    private Storage() {}

    public static void put(String key, String value) {
        storage.put(key, value);
    }

    public static String get(String key) {
        return storage.get(key);
    }
}
