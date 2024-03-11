package dev.khaliuk.ccredis;

public class CommandHandler {
    private static final String CRLF_TERMINATOR = "\r\n";

    private CommandHandler() {
    }

    public static String handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toLowerCase();
        return switch (command) {
            case "ping" -> "+PONG\r\n";
            case "echo" -> "$" + arguments[1].length() + CRLF_TERMINATOR + arguments[1] + CRLF_TERMINATOR;
            default -> throw new RuntimeException("Unknown command: " + command);
        };
    }
}
