package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.command.CommandFactory;

public class CommandHandler {
    private final CommandFactory commandFactory;

    public CommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public byte[] handle(String parsedCommand) {
        String[] arguments = parsedCommand.split(" ");
        String command = arguments[0].toUpperCase();
        return commandFactory.getCommandHandler(command).handle(arguments);
    }
}
