package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

public class ReplConf extends AbstractHandler {
    public ReplConf(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public String handle(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        switch (parameter) {
            case "listening-port":
                objectFactory.getApplicationProperties().addReplica(Integer.parseInt(arguments[2]));
                break;
            case "capa":
                if (!"psync2".equalsIgnoreCase(arguments[2])) {
                    throw new RuntimeException("Unknown parameter: " + arguments[2]);
                }
                break;
            default:
                throw new RuntimeException("Unknown parameter: " + parameter);
        }
        return objectFactory.getProtocolSerializer().simpleString("OK");
    }
}
