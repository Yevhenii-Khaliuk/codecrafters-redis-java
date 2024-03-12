package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;

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

            System.out.println("Server has started on port: " + properties.getPort());

            if (properties.isReplica()) {
                initReplica(objectFactory);
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
        ApplicationProperties.Replica replica = objectFactory.getApplicationProperties().getReplica();

        try (Socket master = new Socket(replica.masterHost(), replica.masterPort());
             OutputStream outputStream = master.getOutputStream();
             DataInputStream inputStream = new DataInputStream(master.getInputStream())
        ) {
            String pingRequest = objectFactory.getProtocolSerializer().array(List.of("PING"));
            outputStream.write(pingRequest.getBytes(StandardCharsets.UTF_8));

        }

    }
}
