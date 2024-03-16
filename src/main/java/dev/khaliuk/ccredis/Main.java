package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.replica.ReplicaRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;

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
                System.out.println("Start replica init");
                new ReplicaRunner(objectFactory).start();
            } else {
                System.out.println(LocalTime.now() + ": Master has started");
            }

            while (true) {
                // TODO: connection handler pool/event loop
                Socket socket = serverSocket.accept();
                new ConnectionHandler(socket, objectFactory).start();
            }
        }
    }
}
