package dev.khaliuk.ccredis.storage;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public record StreamRecord(String id, List<Pair<String, String>> pairs) {
    public List<Object> toSerializable() {
        List<Object> serializableRecord = new ArrayList<>();
        serializableRecord.add(id);
        pairs.forEach(pair -> serializableRecord.add(List.of(pair.getKey(), pair.getValue())));
        return serializableRecord;
    }
}
