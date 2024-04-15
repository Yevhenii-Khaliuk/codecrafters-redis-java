package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
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

    public boolean isAvailable() {
        return available;
    }

    public void send(byte[] command) {
        log(command);
        executorService.execute(() -> doSend(command));
    }

    public synchronized byte[] sendAndAwaitResponse(byte[] command) {
        log(command);
        try {
            return executorService.submit(() -> doSendAndAwait(command)).get();
        } catch (InterruptedException | ExecutionException e) {
            handleError(e);
            throw new RuntimeException(e);
        }
    }

    private void log(byte[] command) {
        LOGGER.log(replica.getPort() + " replica client received command: " +
            new String(command).replace("\r\n", " "));
    }

    private byte[] doSendAndAwait(byte[] command) throws IOException {
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
    }

    private void doSend(byte[] command) {
        try {
            OutputStream outputStream = replica.getOutputStream();
            LOGGER.log("Sending command to output stream");
            outputStream.write(command);
            outputStream.flush();
            LOGGER.log("Command sent");
        } catch (IOException e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        // TODO: error handling should be more granular
        //  and should not mark replica as unavailable for each possible error
        LOGGER.log("Error during execution: " + e.getMessage());
        LOGGER.log("Marking current replica as unavailable");
        available = false;
    }
}
