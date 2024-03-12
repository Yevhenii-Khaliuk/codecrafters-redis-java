package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public class Ping extends AbstractHandler {
    public Ping(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public String handle(String[] arguments) {
        return objectFactory.getProtocolSerializer().simpleString("PONG");
    }
}
