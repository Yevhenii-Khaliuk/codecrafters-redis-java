package dev.khaliuk.ccredis.command;

import java.util.List;

public interface Handler {
    List<String> handle(String[] arguments);
}
