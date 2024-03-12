package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.replica.ReplicaInitializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

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
                new ReplicaInitializer(objectFactory).init();
                System.out.println("Replica is initialized");
            } else {
                System.out.println("Master has started");
            }

            while (true) {
                // TODO: connection handler pool
                Socket socket = serverSocket.accept();
                new ConnectionHandler(socket, objectFactory).start();
            }
        }
    }
}
