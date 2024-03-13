package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class Info extends AbstractHandler {
    public Info(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        return switch (parameter) {
            case "replication" -> objectFactory.getProtocolSerializer().bulkString(getReplicationInfo());
            default -> throw new RuntimeException("Unknown parameter: " + parameter);
        };
    }

    private String getReplicationInfo() {
        List<String> values = new ArrayList<>();
        ApplicationProperties applicationProperties = objectFactory.getApplicationProperties();
        boolean isMaster = applicationProperties.isMaster();
        String role = "role:" + (isMaster ? "master" : "slave");
        values.add(role);
        if (isMaster) {
            values.add("master_replid:" + applicationProperties.getReplicationId());
            values.add("master_repl_offset:" + applicationProperties.getReplicationOffset());
        }
        return String.join("\n", values);
    }
}
