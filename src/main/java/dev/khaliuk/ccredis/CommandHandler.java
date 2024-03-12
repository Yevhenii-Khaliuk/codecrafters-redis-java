package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;
import dev.khaliuk.ccredis.storage.Storage;

public class CommandHandler {
    private final ProtocolSerializer protocolSerializer;
    private final ApplicationProperties applicationProperties;

    public CommandHandler(ProtocolSerializer protocolSerializer, ApplicationProperties applicationProperties) {
        this.protocolSerializer = protocolSerializer;
        this.applicationProperties = applicationProperties;
    }

    public String handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toLowerCase();
        return switch (command) {
            case "ping" -> protocolSerializer.simpleString("PONG");
            case "echo" -> protocolSerializer.bulkString(arguments[1]);
            case "set" -> handleSet(arguments);
            case "get" -> handleGet(arguments);
            case "info" -> handleInfo(arguments);
            default -> throw new RuntimeException("Unknown command: " + command);
        };
    }

    private String handleSet(String[] arguments) {
        if (arguments.length > 3) {
            String parameter = arguments[3].toLowerCase();
            switch (parameter) {
                case "px":
                    Long expiration = Long.parseLong(arguments[4]);
                    Storage.put(arguments[1], arguments[2], expiration);
                    break;
                default:
                    throw new RuntimeException("Unknown parameter: " + parameter);
            }
        } else {
            Storage.put(arguments[1], arguments[2]);
        }
        return protocolSerializer.simpleString("OK");
    }

    private String handleGet(String[] arguments) {
        String value = Storage.get(arguments[1]);
        return protocolSerializer.bulkString(value);
    }

    private String handleInfo(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        String role = "role:" + (applicationProperties.isReplica() ? "slave" : "master");
        return switch (parameter) {
            case "replication" -> protocolSerializer.bulkString(role);
            default -> throw new RuntimeException("Unknown parameter: " + parameter);
        };
    }
}
