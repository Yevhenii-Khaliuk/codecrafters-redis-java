package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Base64;

public class Psync extends AbstractHandler {
    private static final String EMPTY_RDB_FILE =
            "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";

    public Psync(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        String replicationId = objectFactory.getApplicationProperties().getReplicationId();
        Long replicationOffset = objectFactory.getApplicationProperties().getReplicationOffset();
        String fullResync = String.format("FULLRESYNC %s %s", replicationId, replicationOffset);
        byte[] fullResyncResponse = objectFactory.getProtocolSerializer().simpleString(fullResync);
        byte[] rdbFile = Base64.getDecoder().decode(EMPTY_RDB_FILE);
        byte[] sizePrefix = ("$" + rdbFile.length + "\r\n").getBytes();
        byte[] response = ArrayUtils.addAll(fullResyncResponse, sizePrefix);
        return ArrayUtils.addAll(response, rdbFile);
    }
}
