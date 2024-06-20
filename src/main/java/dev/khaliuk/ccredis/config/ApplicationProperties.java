package dev.khaliuk.ccredis.config;

import dev.khaliuk.ccredis.replica.ReplicaClient;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApplicationProperties {
    private int port = 6379;
    private ReplicaProperties replicaProperties;

    // Master properties
    private String replicationId;
    private Long replicationOffset = 0L;
    private List<ReplicaClient> replicas;

    // RDB file properties
    private String dir;
    private String dbFilename;

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

    public void setReplicationOffset(Long replicationOffset) {
        this.replicationOffset = replicationOffset;
    }

    public synchronized List<ReplicaClient> getReplicas() {
        if (replicas == null) {
            replicas = new ArrayList<>();
        }
        return replicas;
    }

    public synchronized void addReplica(ReplicaClient replica) {
        if (replicas == null) {
            replicas = new ArrayList<>();
        }
        replicas.add(replica);
    }

    public String getDir() {
        return dir;
    }

    public String getDbFilename() {
        return dbFilename;
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
                    var hostPort = args[i + 1].split(" ");
                    replicaProperties = new ReplicaProperties(hostPort[0], Integer.parseInt(hostPort[1]));
                    i += 2;
                    break;
                case "dir":
                    dir = args[i + 1];
                    i++;
                    break;
                case "dbfilename":
                    dbFilename = args[i + 1];
                    i++;
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
