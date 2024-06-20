package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.List;

public class Config extends AbstractHandler {

    public static final String DIR = "dir";
    public static final String DB_FILENAME = "dbfilename";

    public Config(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        switch (parameter) {
            case "get":
                parameter = arguments[2].toLowerCase();
                return switch (parameter) {
                    case DIR -> {
                        var dir = objectFactory.getApplicationProperties().getDir();
                        yield protocolSerializer().array(List.of(DIR, dir));
                    }
                    case DB_FILENAME -> {
                        var dbFilename = objectFactory.getApplicationProperties().getDir();
                        yield protocolSerializer().array(List.of(DB_FILENAME, dbFilename));
                    }
                    default -> throw new RuntimeException("Unknown parameter: " + parameter);
                };
            default:
                throw new RuntimeException("Unknown parameter: " + parameter);
        }
    }
}
