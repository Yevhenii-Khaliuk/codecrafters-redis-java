package dev.khaliuk.ccredis;

public class CommandHandler {
    private static final String CRLF_TERMINATOR = "\r\n";

    private CommandHandler() {
    }

    public static String handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toLowerCase();
        return switch (command) {
            case "ping" -> "+PONG" + CRLF_TERMINATOR;
            case "echo" -> "$" + arguments[1].length() + CRLF_TERMINATOR + arguments[1] + CRLF_TERMINATOR;
            case "set" -> handleSet(arguments);
            case "get" -> handleGet(arguments);
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
        return "+OK" + CRLF_TERMINATOR;
    }

    private static String handleGet(String[] arguments) {
        String value = Storage.get(arguments[1]);
        if (value == null) {
            return "$-1" + CRLF_TERMINATOR;
        }
        return "$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR;
    }
}
