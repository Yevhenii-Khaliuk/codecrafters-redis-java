package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReplicaClient {
    private static final Logger LOGGER = new Logger(ReplicaClient.class);

    private volatile boolean available = true;

    private final Socket replica;
    private final ProtocolDeserializer protocolDeserializer;
    private final ExecutorService executorService;

    public ReplicaClient(Socket replica, ProtocolDeserializer protocolDeserializer) {
        this.replica = replica;
        this.protocolDeserializer = protocolDeserializer;
        executorService = Executors.newSingleThreadExecutor();
    }

    public int getPort() {
        return replica.getPort();
    }

    public boolean isAvailable() {
        return available;
    }

    public void send(byte[] command) {
        LOGGER.log(replica.getPort() + " replica client received command: " +
                new String(command).replace("\r\n", " "));

//        try {
            executorService.execute(() -> doSend(command));
//        } catch (InterruptedException | ExecutionException e) {
//            handleError(e);
//            throw new RuntimeException(e);
//        }
    }

    public synchronized byte[] sendAndAwaitResponse(byte[] command) {
        LOGGER.log(replica.getPort() + " replica client received command: " +
                new String(command).replace("\r\n", " "));

        try {
            return executorService.submit(() -> doSendAndAwait(command)).get();
        } catch (InterruptedException | ExecutionException e) {
            handleError(e);
            throw new RuntimeException(e);
        }
    }

    private void handleError(Exception e) {
        // TODO: error handling should be more granular
        //  and should not mark replica as unavailable for each possible error
        LOGGER.log("Error during execution: " + e.getMessage());
        LOGGER.log("Marking current replica as unavailable");
        available = false;
    }

    private byte[] doSendAndAwait(byte[] command) {
        try {
            OutputStream outputStream = replica.getOutputStream();
            LOGGER.log("Sending command to output stream");
            outputStream.write(command);
            outputStream.flush();
            LOGGER.log("Command sent");
            DataInputStream inputStream = new DataInputStream(replica.getInputStream());
            LOGGER.log("Waiting for response from input stream");
            String response = protocolDeserializer.parseInput(inputStream).getLeft();
            LOGGER.log("Response received");
            return response.getBytes();
        } catch (IOException e) {
            LOGGER.log("Unexpected error: " + e);
        }
        return new byte[]{};
    }

    private void doSend(byte[] command) {
        try {
            OutputStream outputStream = replica.getOutputStream();
            LOGGER.log("Sending command to output stream");
            outputStream.write(command);
            outputStream.flush();
            LOGGER.log("Command sent");
        } catch (IOException e) {
            LOGGER.log("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplicaClient that = (ReplicaClient) o;
        return Objects.equals(replica, that.replica);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(replica);
    }
}
