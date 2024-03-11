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
            case "set" -> {
                Storage.put(arguments[1], arguments[2]);
                yield "+OK" + CRLF_TERMINATOR;
            }
            case "get" -> {
                String value = Storage.get(arguments[1]);
                yield "$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR;
            }
            default -> throw new RuntimeException("Unknown command: " + command);
        };
    }
}
