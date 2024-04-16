package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StreamRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Xrange extends AbstractHandler {
    public Xrange(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        if (arguments.length != 4) {
            throw new IllegalArgumentException("Expected 4 arguments, got " + arguments.length);
        }

        String streamKey = arguments[1];
        String start = arguments[2];
        String end = arguments[3];

        List foundStream =
            Optional.ofNullable(Storage.get(streamKey))
                .filter(r -> r.valueType() == ValueType.STREAM)
                .map(r -> (List<StreamRecord>) r.value())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(r -> idIsBetween(r.id(), start, end))
                .map(this::mapToSerializable)
                .toList();


        return objectFactory.getProtocolSerializer().array(foundStream);
    }

    private boolean idIsBetween(String id, String start, String end) {
        return id.compareTo(start) >= 0 && id.compareTo(end) <= 0;
    }

    private List<Object> mapToSerializable(StreamRecord streamRecord) {
        List<Object> serializableRecord = new ArrayList<>();
        serializableRecord.add(streamRecord.id());
        streamRecord.pairs()
            .forEach(pair -> serializableRecord.add(List.of(pair.getKey(), pair.getValue())));
        return serializableRecord;
    }
}
