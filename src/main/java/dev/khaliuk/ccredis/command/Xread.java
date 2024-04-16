package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StreamRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Xread extends AbstractHandler {
    public Xread(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        validateArguments(arguments);

        String streamKey = arguments[2];
        String startId = arguments[3];

        List foundStream =
            Optional.ofNullable(Storage.get(streamKey))
                .filter(r -> r.valueType() == ValueType.STREAM)
                .map(r -> (List<StreamRecord>) r.value())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(r -> r.id().compareTo(startId) > 0)
                .map(StreamRecord::toSerializable)
                .toList();


        return objectFactory.getProtocolSerializer().array(List.of(List.of(streamKey, foundStream)));
    }

    private void validateArguments(String[] arguments) {
        if (arguments.length < 4) {
            throw new IllegalArgumentException("Expected at least 4 arguments, got " + arguments.length);
        }
        if (!arguments[1].equalsIgnoreCase("streams")) {
            throw new IllegalArgumentException("Expected 'streams' argument, got " + arguments[1]);
        }
    }
}
