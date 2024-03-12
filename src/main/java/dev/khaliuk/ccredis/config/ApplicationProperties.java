package dev.khaliuk.ccredis.config;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApplicationProperties {
    private static final Object LOCK = new Object();

    private String host;
    private int port = 6379;
    private ReplicaProperties replicaProperties;

    // Master properties
    private String replicationId;
    private Long replicationOffset;
    private List<ReplicaProperties> replicas;

    public ApplicationProperties(String[] args) {
        parseArgs(args);
        if (isMaster()) {
            setMasterProperties();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public boolean isReplica() {
        return replicaProperties != null;
    }

    public boolean isMaster() {
        return replicaProperties == null;
    }

    public ReplicaProperties getReplica() {
        return replicaProperties;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public Long getReplicationOffset() {
        return replicationOffset;
    }

    public List<ReplicaProperties> getReplicas() {
        return replicas;
    }

    public synchronized void addReplica(int port) {
        if (replicas == null) {
            replicas = new ArrayList<>();
        }
        ReplicaProperties newReplica = new ReplicaProperties(host, port);
        System.out.println("Replica added: " + newReplica);
        replicas.add(newReplica);
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
                    replicaProperties = new ReplicaProperties(args[i + 1], Integer.parseInt(args[i + 2]));
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
}
