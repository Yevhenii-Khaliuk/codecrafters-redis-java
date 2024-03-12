package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.command.CommandFactory;

import java.util.List;

public class CommandHandler {
    private final CommandFactory commandFactory;

    public CommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public List<String> handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toUpperCase();
        List<String> responses = commandFactory.getCommandHandler(command).handle(arguments);
        System.out.println("Debug response: " + responses);
        return responses;
    }
}
