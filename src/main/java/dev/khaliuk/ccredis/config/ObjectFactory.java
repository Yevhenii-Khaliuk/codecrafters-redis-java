package dev.khaliuk.ccredis.config;

import dev.khaliuk.ccredis.CommandHandler;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;

public class ObjectFactory {
    private final ApplicationProperties applicationProperties;

    private ProtocolDeserializer protocolDeserializer;
    private ProtocolSerializer protocolSerializer;
    private CommandHandler commandHandler;

    public ObjectFactory(ApplicationProperties applicationProperties) {
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

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    private void init() {
        protocolDeserializer = new ProtocolDeserializer();
        protocolSerializer = new ProtocolSerializer();
        commandHandler = new CommandHandler(protocolSerializer, applicationProperties);
    }
}
