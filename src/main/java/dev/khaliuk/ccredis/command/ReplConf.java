package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public class ReplConf extends AbstractHandler {
    public ReplConf(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String parameter = arguments[1].toLowerCase();
        switch (parameter) {
            case "listening-port":
                if (!NumberUtils.isDigits(arguments[2])) {
                    throw new RuntimeException("Port cannot be parsed: " + arguments[2]);
                }
                break;
            case "capa":
                if (!java.util.Set.of("psync2", "eof").contains(arguments[2])) {
                    throw new RuntimeException("Unknown parameter: " + arguments[2]);
                }
                break;
            case "getack":
                var offset = objectFactory.getApplicationProperties().getReplicationOffset();
                return objectFactory.getProtocolSerializer().array(List.of("REPLCONF", "ACK", offset.toString()));
            default:
                throw new RuntimeException("Unknown parameter: " + parameter);
        }
        return objectFactory.getProtocolSerializer().simpleString("OK");
    }
}
