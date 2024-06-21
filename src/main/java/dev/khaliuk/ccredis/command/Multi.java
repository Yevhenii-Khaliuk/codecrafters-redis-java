package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class Multi extends AbstractHandler {
    private final List<String[]> commandsCache = new ArrayList<>();

    public Multi(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        return protocolSerializer().simpleString("OK");
    }

    public byte[] enqueueCommand(String[] command) {
        commandsCache.add(command);
        return protocolSerializer().simpleString("QUEUED");
    }

    public byte[] executeCommands() {
        if (commandsCache.isEmpty()) {
            return protocolSerializer().array(List.of());
        } else {
            // same response for now
            return protocolSerializer().array(List.of());
        }
    }
}
