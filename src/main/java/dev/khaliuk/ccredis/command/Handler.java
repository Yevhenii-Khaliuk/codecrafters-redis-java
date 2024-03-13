package dev.khaliuk.ccredis.command;

public interface Handler {
    byte[] handle(String[] arguments);
}
