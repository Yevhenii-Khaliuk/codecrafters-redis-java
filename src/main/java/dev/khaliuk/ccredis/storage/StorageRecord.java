package dev.khaliuk.ccredis.storage;

import java.time.Instant;

public record StorageRecord(String value, Instant expiration) {
}
