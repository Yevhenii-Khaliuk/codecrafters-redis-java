package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.List;

public class Ping extends AbstractHandler {
    public Ping(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public List<String> handle(String[] arguments) {
        return List.of(objectFactory.getProtocolSerializer().simpleString("PONG"));
    }
}
