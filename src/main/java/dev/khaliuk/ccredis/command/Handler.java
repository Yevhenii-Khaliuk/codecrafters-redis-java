package dev.khaliuk.ccredis.command;

public interface Handler {
    String handle(String[] arguments);
}
