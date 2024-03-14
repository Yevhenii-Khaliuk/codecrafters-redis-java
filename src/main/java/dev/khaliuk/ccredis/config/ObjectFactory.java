package dev.khaliuk.ccredis.config;

import dev.khaliuk.ccredis.command.CommandFactory;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;
import dev.khaliuk.ccredis.replica.CommandReplicator;

import java.lang.reflect.InvocationTargetException;

public class ObjectFactory {
    private final ApplicationProperties applicationProperties;
    private ProtocolDeserializer protocolDeserializer;
    private ProtocolSerializer protocolSerializer;
    private CommandFactory commandFactory;
    private CommandReplicator commandReplicator;

    public ObjectFactory(ApplicationProperties applicationProperties) throws InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.applicationProperties = applicationProperties;
        init();
    }

    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    public ProtocolDeserializer getProtocolDeserializer() {
        return protocolDeserializer;
    }

    public ProtocolSerializer getProtocolSerializer() {
        return protocolSerializer;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public CommandReplicator getCommandReplicator() {
        return commandReplicator;
    }

    private void init() throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        protocolDeserializer = new ProtocolDeserializer();
        protocolSerializer = new ProtocolSerializer();
        commandFactory = new CommandFactory(this);
        commandReplicator = new CommandReplicator(this);
    }
}
