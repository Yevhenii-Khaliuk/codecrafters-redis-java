package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;

public abstract class AbstractHandler implements Handler {
    protected ObjectFactory objectFactory;

    protected AbstractHandler(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    protected ProtocolSerializer protocolSerializer() {
        return objectFactory.getProtocolSerializer();
    }
}
