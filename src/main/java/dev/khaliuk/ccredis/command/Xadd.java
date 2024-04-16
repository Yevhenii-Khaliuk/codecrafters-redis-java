package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Xadd extends AbstractHandler {
    public Xadd(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String streamKey = arguments[1];
        String streamId = arguments[2];

        List<Pair<String, String>> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(Pair.of("id", streamId));

        for (int i = 3; i < arguments.length - 1; i += 2) {
            keyValuePairs.add(Pair.of(arguments[i], arguments[i + 1]));
        }

        Storage.put(streamKey, ValueType.STREAM, keyValuePairs);

        return objectFactory.getProtocolSerializer().bulkString(streamId);
    }
}
