package dev.khaliuk.ccredis.config;

public class ApplicationProperties {
    private int port = 6379;
    private Replica replica;

    public int getPort() {
        return port;
    }

    public boolean isReplica() {
        return replica != null;
    }

    public Replica getReplica() {
        return replica;
    }

    public ApplicationProperties(String[] args) {
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

    public record Replica(String host, int port) {
    }
}
