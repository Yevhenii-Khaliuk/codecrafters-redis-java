package dev.khaliuk.ccredis.storage;

import dev.khaliuk.ccredis.protocol.ValueType;

import java.time.Instant;

public record StorageRecord(ValueType valueType, Object value, Instant expiration) {
}
