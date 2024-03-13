package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.config.ReplicaProperties;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ReplicaInitializer {
    private final ApplicationProperties applicationProperties;
    private final ProtocolSerializer protocolSerializer;
    private final ProtocolDeserializer protocolDeserializer;

    public ReplicaInitializer(ObjectFactory objectFactory) {
        this.applicationProperties = objectFactory.getApplicationProperties();
        this.protocolSerializer = objectFactory.getProtocolSerializer();
        this.protocolDeserializer = objectFactory.getProtocolDeserializer();
    }

    public void init() throws IOException {
        ReplicaProperties replicaProperties = applicationProperties.getReplica();

        try (Socket master = new Socket(replicaProperties.host(), replicaProperties.port());
             OutputStream outputStream = master.getOutputStream();
             DataInputStream inputStream = new DataInputStream(master.getInputStream())
        ) {
            // Step 1: PING
            System.out.println("Sending PING request");
            byte[] request = protocolSerializer.array(List.of("PING"));
            outputStream.write(request);
            String response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("PONG")) {
                throw new RuntimeException("Unexpected response for PING: " + response);
            }
            // Step 2: REPLCONF listening-port port
            System.out.println("Sending REPLCONF port request");
            request = protocolSerializer.array(
                    List.of("REPLCONF", "listening-port", String.valueOf(applicationProperties.getPort())));
            outputStream.write(request);
            response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("OK")) {
                throw new RuntimeException("Unexpected response for REPLCONF port: " + response);
            }
            // Step 3: REPLCONF capa psync2
            System.out.println("Sending REPLCONF capa request");
            request = protocolSerializer.array(List.of("REPLCONF", "capa", "psync2"));
            outputStream.write(request);
            response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("OK")) {
                throw new RuntimeException("Unexpected response for REPLCONF capa: " + response);
            }
            // Step 4: PSYNC ? -1
            System.out.println("Sending PSYNC request");
            request = protocolSerializer.array(List.of("PSYNC", "?", "-1"));
            outputStream.write(request);
            response = protocolDeserializer.parseInput(inputStream);
            if (!response.startsWith("FULLRESYNC")) {
                throw new RuntimeException("Unexpected response for PSYNC: " + response);
            }
        }
    }
}
