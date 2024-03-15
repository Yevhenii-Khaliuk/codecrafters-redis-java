package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

import java.io.IOException;

public class Get extends AbstractHandler {
    public Get(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String value;
        if (objectFactory.getApplicationProperties().getDir() == null) {
            value = Storage.get(arguments[1]);
        } else {
            try {
                value = new String(objectFactory.getRdbProcessor().readFirstKeyValuePair().getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return objectFactory.getProtocolSerializer().bulkString(value);
    }
}
