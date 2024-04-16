package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StreamRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Xread extends AbstractHandler {
    public Xread(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        // TODO: validate arguments

        int streamsIndex = getStreamsIndex(arguments);
        var keysIdsNumber = (arguments.length - streamsIndex - 1) / 2;
        List<Pair<String, String>> keyIdPairs = parseKeyIdPairs(arguments, streamsIndex, keysIdsNumber);
        List result = keyIdPairs.stream()
            .map(this::toSerializableStreams)
            .toList();

        return objectFactory.getProtocolSerializer().array(result);
    }

    private int getStreamsIndex(String[] arguments) {
        for (int i = 1; i < arguments.length - 2; i++) {
            if (arguments[i].equals("streams")) {
                return i;
            }
        }

        throw new IllegalArgumentException("Expected 'streams' argument");
    }

    private List<Pair<String, String>> parseKeyIdPairs(String[] arguments, int streamsIndex, int keysIdsNumber) {
        List<Pair<String, String>> keyIdPairs = new ArrayList<>();
        int firstKeyIndex = streamsIndex + 1;
        for (int i = firstKeyIndex; i < firstKeyIndex + keysIdsNumber; i++) {
            keyIdPairs.add(Pair.of(arguments[i], arguments[i + keysIdsNumber]));
        }
        return keyIdPairs;
    }

    private List toSerializableStreams(Pair<String, String> keyIdPair) {
        String streamKey = keyIdPair.getKey();
        String startId = keyIdPair.getValue();

        List foundStream =
            Optional.ofNullable(Storage.get(streamKey))
                .filter(r -> r.valueType() == ValueType.STREAM)
                .map(r -> (List<StreamRecord>) r.value())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(r -> r.id().compareTo(startId) > 0)
                .map(StreamRecord::toSerializable)
                .toList();

        return List.of(streamKey, foundStream);
    }
}
