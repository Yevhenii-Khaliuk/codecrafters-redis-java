package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.command.CommandFactory;

public class CommandHandler {
    private final CommandFactory commandFactory;

    public CommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public String handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toUpperCase();
        String response = commandFactory.getCommandHandler(command).handle(arguments);
        System.out.println("Debug response: " + response);
        return response;
    }
}
