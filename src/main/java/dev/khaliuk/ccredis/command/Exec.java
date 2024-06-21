package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public class Exec extends AbstractHandler {
    private static final String EXEC_WITHOUT_MULTI_ERROR_MESSAGE = "ERR EXEC without MULTI";

    public Exec(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        return protocolSerializer().simpleError(EXEC_WITHOUT_MULTI_ERROR_MESSAGE);
    }
}
