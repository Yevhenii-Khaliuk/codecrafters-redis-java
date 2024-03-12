package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.util.Base64;
import java.util.List;

public class Psync extends AbstractHandler {
    private static final String EMPTY_RDB_FILE =
            "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";

    public Psync(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public List<String> handle(String[] arguments) {
        String replicationId = objectFactory.getApplicationProperties().getReplicationId();
        Long replicationOffset = objectFactory.getApplicationProperties().getReplicationOffset();
        String response = String.format("FULLRESYNC %s %s", replicationId, replicationOffset);
        String rdbFile = new String(Base64.getDecoder().decode(EMPTY_RDB_FILE));
        return List.of(
                objectFactory.getProtocolSerializer().simpleString(response),
                objectFactory.getProtocolSerializer().bulkStringNoTrailingTerminator(rdbFile));
    }
}
