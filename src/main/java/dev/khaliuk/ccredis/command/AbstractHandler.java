package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public abstract class AbstractHandler implements Handler {
    protected ObjectFactory objectFactory;

    protected AbstractHandler(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
}
