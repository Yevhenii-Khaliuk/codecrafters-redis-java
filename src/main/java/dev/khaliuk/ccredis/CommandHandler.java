package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.protocol.ProtocolSerializer;

public class CommandHandler {
    private CommandHandler() {
    }

    public static String handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toLowerCase();
        return switch (command) {
            case "ping" -> ProtocolSerializer.simpleString("PONG");
            case "echo" -> ProtocolSerializer.bulkString(arguments[1]);
            case "set" -> handleSet(arguments);
            case "get" -> handleGet(arguments);
            case "info" -> handleInfo(arguments);
            default -> throw new RuntimeException("Unknown command: " + command);
        };
    }

    private static String handleSet(String[] arguments) {
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
        return ProtocolSerializer.simpleString("OK");
    }

    private static String handleGet(String[] arguments) {
        String value = Storage.get(arguments[1]);
        return ProtocolSerializer.bulkString(value);
    }

    private static String handleInfo(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        String role = "role:master";
        return switch (parameter) {
            case "replication" -> ProtocolSerializer.bulkString(role);
            default -> throw new RuntimeException("Unknown parameter: " + parameter);
        };
    }
}
