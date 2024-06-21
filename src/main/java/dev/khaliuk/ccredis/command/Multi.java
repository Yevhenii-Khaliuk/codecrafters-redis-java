package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class Multi extends AbstractHandler {
    private final ThreadLocal<List<String[]>> commandsCache = new ThreadLocal<>();

    public Multi(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        return protocolSerializer().simpleString("OK");
    }

    public byte[] enqueueCommand(String[] command) {
        if (commandsCache.get() == null) {
            commandsCache.set(new ArrayList<>());
        }

        commandsCache.get().add(command);
        return protocolSerializer().simpleString("QUEUED");
    }

    public byte[] executeCommands() {
        if (commandsCache.get() == null) {
            return protocolSerializer().array(List.of());
        } else {
            var responses = commandsCache.get()
                .stream()
                .map(command -> objectFactory.getCommandFactory()
                    .getCommandHandler(command[0])
                    .handle(command))
                .toList();
            commandsCache.remove();
            return protocolSerializer().arrayOfSerialized(responses);
        }
    }

    public byte[] discardTransaction() {
        commandsCache.remove();
        return protocolSerializer().simpleString("OK");
    }
}
