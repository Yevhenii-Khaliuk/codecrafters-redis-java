package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

import java.util.List;

public class Get extends AbstractHandler {
    public Get(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public List<String> handle(String[] arguments) {
        String value = Storage.get(arguments[1]);
        return List.of(objectFactory.getProtocolSerializer().bulkString(value));
    }
}
