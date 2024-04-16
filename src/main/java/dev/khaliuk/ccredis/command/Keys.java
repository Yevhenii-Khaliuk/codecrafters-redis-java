package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.io.IOException;
import java.util.List;

public class Keys extends AbstractHandler {
    public Keys(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        List<byte[]> keys;
        try {
            keys = objectFactory.getRdbProcessor().readAllKeys();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return objectFactory.getProtocolSerializer().array(keys.stream().map(String::new).toList());
    }
}
