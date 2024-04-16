package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StorageRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Xadd extends AbstractHandler {
    private static final Logger LOGGER = new Logger(Xadd.class);

    public Xadd(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String streamKey = arguments[1];
        String streamId = arguments[2];

        List<Pair<String, String>> existingStream =
            (List<Pair<String, String>>) Optional.ofNullable(Storage.get(streamKey))
                .filter(r -> r.valueType() == ValueType.STREAM)
                .map(StorageRecord::value)
                .orElseGet(ArrayList::new);

        String latestId = existingStream.reversed().stream()
            .filter(p -> p.getKey().equals("id"))
            .findFirst()
            .map(Pair::getValue)
            .orElse(null);

        String errorMessage = validateStreamId(streamId, latestId);
        if (errorMessage != null) {
            return objectFactory.getProtocolSerializer().simpleError(errorMessage);
        }

        List<Pair<String, String>> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(Pair.of("id", streamId));

        for (int i = 3; i < arguments.length - 1; i += 2) {
            keyValuePairs.add(Pair.of(arguments[i], arguments[i + 1]));
        }

        existingStream.addAll(keyValuePairs);
        Storage.put(streamKey, ValueType.STREAM, existingStream);

        return objectFactory.getProtocolSerializer().bulkString(streamId);
    }

    private String validateStreamId(String newId, String existingId) {
        long newMillisecondsTime = Long.parseLong(newId.substring(0, newId.indexOf("-")));
        long newSequenceNumber = Long.parseLong(newId.substring(newId.indexOf("-") + 1));

        if (newMillisecondsTime == 0 && newSequenceNumber == 0) {
            return "ERR The ID specified in XADD must be greater than 0-0";
        }

        if (existingId == null) {
            return null;
        }

        long existingMillisecondsTime = Long.parseLong(existingId.substring(0, existingId.indexOf("-")));
        long existingSequenceNumber = Long.parseLong(existingId.substring(existingId.indexOf("-") + 1));

        if (newMillisecondsTime < existingMillisecondsTime) {
            return "ERR The ID specified in XADD is equal or smaller than the target stream top item";
        }

        if (newMillisecondsTime == existingMillisecondsTime && newSequenceNumber <= existingSequenceNumber) {
            return "ERR The ID specified in XADD is equal or smaller than the target stream top item";
        }

        return null;
    }
}
