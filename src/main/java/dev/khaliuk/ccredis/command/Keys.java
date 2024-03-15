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
        byte[] firstKey;
        try {
            firstKey = objectFactory.getRdbProcessor().readFirstKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return objectFactory.getProtocolSerializer().array(List.of(new String(firstKey)));
    }
}
