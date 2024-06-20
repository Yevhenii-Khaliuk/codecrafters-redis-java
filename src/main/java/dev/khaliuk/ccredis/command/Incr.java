package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

public class Incr extends AbstractHandler implements Write {
    private static final String NOT_AN_INTEGER_ERROR_MESSAGE = "ERR value is not an integer or out of range";

    public Incr(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        var key = arguments[1];
        long value;

        if (Storage.contains(key)) {
            try {
                var storageValue = (String) Storage.get(key).value();
                value = Long.parseLong(storageValue) + 1;
            } catch (NumberFormatException e) {
                return objectFactory.getProtocolSerializer().simpleError(NOT_AN_INTEGER_ERROR_MESSAGE);
            }
        } else {
            value = 1;
        }

        Storage.put(key, String.valueOf(value));
        return objectFactory.getProtocolSerializer().integer(value);
    }
}
