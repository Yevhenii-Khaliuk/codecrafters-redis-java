package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public class Echo extends AbstractHandler {
    public Echo(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public String handle(String[] arguments) {
        return objectFactory.getProtocolSerializer().bulkString(arguments[1]);
    }
}
