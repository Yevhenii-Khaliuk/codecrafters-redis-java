package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

public class Set extends AbstractHandler implements Write {
    public Set(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
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
        return objectFactory.getProtocolSerializer().simpleString("OK");
    }
}
