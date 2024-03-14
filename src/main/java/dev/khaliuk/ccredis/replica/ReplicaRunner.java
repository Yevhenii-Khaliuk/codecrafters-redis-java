package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.command.CommandFactory;
import dev.khaliuk.ccredis.command.Handler;
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

public class ReplicaRunner extends Thread {
    private final ApplicationProperties applicationProperties;
    private final ProtocolSerializer protocolSerializer;
    private final ProtocolDeserializer protocolDeserializer;
    private final CommandFactory commandFactory;

    public ReplicaRunner(ObjectFactory objectFactory) {
        applicationProperties = objectFactory.getApplicationProperties();
        protocolSerializer = objectFactory.getProtocolSerializer();
        protocolDeserializer = objectFactory.getProtocolDeserializer();
        commandFactory = objectFactory.getCommandFactory();
    }

    @Override
    public void run() {
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

            protocolDeserializer.parseRdbFile(inputStream);

            System.out.println("Replica has started");

            while (true) {
                String commandString = protocolDeserializer.parseInput(inputStream);
                System.out.println("Replica has received replication command: " + commandString);
                String[] arguments = commandString.split(" ");
                String command = arguments[0].toUpperCase();
                Handler handler = commandFactory.getCommandHandler(command);
                handler.handle(arguments);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
