package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StorageRecord;

public class Type extends AbstractHandler {
    public Type(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        StorageRecord storageRecord = Storage.get(arguments[1]);
        String type;
        if (storageRecord == null) {
            type = ValueType.NONE.getDisplay();
        } else {
            type = storageRecord.valueType().getDisplay();
        }
        return objectFactory.getProtocolSerializer().simpleString(type);
    }
}
