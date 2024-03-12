package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.config.ReplicaProperties;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;
import dev.khaliuk.ccredis.protocol.ProtocolSerializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        ApplicationProperties properties = new ApplicationProperties(args);
        ObjectFactory objectFactory = new ObjectFactory(properties);

        try (ServerSocket serverSocket = new ServerSocket(properties.getPort())) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            properties.setHost(serverSocket.getInetAddress().getHostName());

            System.out.println("Server has started on port: " + properties.getPort());

            if (properties.isReplica()) {
                System.out.println("Start replica init");
                initReplica(objectFactory);
                System.out.println("Replica is initialized");
            }

            while (true) {
                // TODO: connection handler pool
                new ConnectionHandler(
                        serverSocket.accept(),
                        objectFactory.getProtocolDeserializer(),
                        objectFactory.getCommandHandler()
                ).start();
            }
        }
    }

    private static void initReplica(ObjectFactory objectFactory) throws IOException {
        ApplicationProperties applicationProperties = objectFactory.getApplicationProperties();
        ReplicaProperties replicaProperties = applicationProperties.getReplica();

        try (Socket master = new Socket(replicaProperties.host(), replicaProperties.port());
             OutputStream outputStream = master.getOutputStream();
             DataInputStream inputStream = new DataInputStream(master.getInputStream())
        ) {
            ProtocolSerializer protocolSerializer = objectFactory.getProtocolSerializer();
            ProtocolDeserializer protocolDeserializer = objectFactory.getProtocolDeserializer();

            // Step 1: PING
            System.out.println("Sending PING request");
            String request = protocolSerializer.array(List.of("PING"));
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            String response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("PONG")) {
                throw new RuntimeException("Unexpected response for PING: " + response);
            }
            // Step 2: REPLCONF listening-port port
            System.out.println("Sending REPLCONF port request");
            request = protocolSerializer.array(
                    List.of("REPLCONF", "listening-port", String.valueOf(applicationProperties.getPort())));
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("OK")) {
                throw new RuntimeException("Unexpected response for REPLCONF port: " + response);
            }
            // Step 3: REPLCONF capa psync2
            System.out.println("Sending REPLCONF capa request");
            request = protocolSerializer.array(
                    List.of("REPLCONF", "capa", "psync2"));
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            response = protocolDeserializer.parseInput(inputStream);
            if (!response.equalsIgnoreCase("OK")) {
                throw new RuntimeException("Unexpected response for REPLCONF port: " + response);
            }
        }
    }
}
