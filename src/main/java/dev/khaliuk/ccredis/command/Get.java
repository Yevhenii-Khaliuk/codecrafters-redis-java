package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.storage.Storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class Get extends AbstractHandler {
    private static final Logger LOGGER = new Logger(Get.class);

    public Get(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        LOGGER.log("Arguments: " + Arrays.toString(arguments));
        String value;
        if (objectFactory.getApplicationProperties().getDir() == null) {
            value = Storage.get(arguments[1]);
        } else {
            try {
                Map<String, String> pairs = objectFactory.getRdbProcessor().readAllPairs();
                LOGGER.log("Pairs read: " + pairs);
                value = pairs.get(arguments[1]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return objectFactory.getProtocolSerializer().bulkString(value);
    }
}
