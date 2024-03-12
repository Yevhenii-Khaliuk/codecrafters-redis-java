package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

public class Get extends AbstractHandler {
    public Get(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public String handle(String[] arguments) {
        String value = Storage.get(arguments[1]);
        return objectFactory.getProtocolSerializer().bulkString(value);
    }
}
