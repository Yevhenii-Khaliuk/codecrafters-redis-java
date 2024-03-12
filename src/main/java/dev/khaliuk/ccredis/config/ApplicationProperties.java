package dev.khaliuk.ccredis.config;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class ApplicationProperties {
    private int port = 6379;
    private Replica replica;

    // Master properties
    private String replicationId;
    private Long replicationOffset;

    public ApplicationProperties(String[] args) {
        parseArgs(args);
        if (isMaster()) {
            setMasterProperties();
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isReplica() {
        return replica != null;
    }

    public boolean isMaster() {
        return replica == null;
    }

    public Replica getReplica() {
        return replica;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public Long getReplicationOffset() {
        return replicationOffset;
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String parameter = args[i].toLowerCase().substring(2);
            switch (parameter) {
                case "port":
                    port = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "replicaof":
                    replica = new Replica(args[i + 1], Integer.parseInt(args[i + 2]));
                    i += 2;
                    break;
                default:
                    throw new RuntimeException("Unknown parameter: " + parameter);
            }
        }
    }

    private void setMasterProperties() {
        replicationId = DigestUtils.sha1Hex(UUID.randomUUID().toString());
        replicationOffset = 0L;
    }

    public record Replica(String masterHost, int masterPort) {
    }
}
