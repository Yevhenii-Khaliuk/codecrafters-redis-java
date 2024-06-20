package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.List;

public class Echo extends AbstractHandler {
    public Echo(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        return protocolSerializer().bulkString(arguments[1]);
    }
}
