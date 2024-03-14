package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.config.ObjectFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class CommandReplicator {
    private final ObjectFactory objectFactory;

    public CommandReplicator(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void replicateWriteCommand(String commandString) {
        objectFactory.getApplicationProperties()
                .getReplicas()
                .forEach(replica -> replicate(replica, commandString));
    }

    private void replicate(Socket replica, String commandString) {
        try  {
            OutputStream outputStream = replica.getOutputStream();
            byte[] request = objectFactory.getProtocolSerializer().array(List.of(commandString.split(" ")));
            outputStream.write(request);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Replication error: " + e.getMessage());
        }
    }
}
