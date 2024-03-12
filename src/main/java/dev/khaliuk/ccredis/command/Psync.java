package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public class Psync extends AbstractHandler {
    public Psync(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public String handle(String[] arguments) {
        String replicationId = objectFactory.getApplicationProperties().getReplicationId();
        Long replicationOffset = objectFactory.getApplicationProperties().getReplicationOffset();
        String response = String.format("FULLRESYNC %s %s", replicationId, replicationOffset);
        return objectFactory.getProtocolSerializer().simpleString(response);
    }
}
