package dev.khaliuk.ccredis.replica;

import dev.khaliuk.ccredis.config.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReplicaClient {
    private static final Logger LOGGER = new Logger(ReplicaClient.class);

    private final Socket replica;
    private final ExecutorService executorService;

    public ReplicaClient(Socket replica) {
        this.replica = replica;
        executorService = Executors.newSingleThreadExecutor();
    }

    public void send(byte[] command) {
        log(command);
        executorService.execute(() -> doSend(command));
    }

    public synchronized byte[] sendAndAwaitResponse(byte[] command, long timeout) {
        log(command);
        Future<byte[]> future = executorService.submit(() -> doSendAndAwait(command));
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            LOGGER.log("Cancelling task");
            future.cancel(true);
            LOGGER.log("Task is cancelled");
            throw new RuntimeException("Operation timed out");
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(replica.getInputStream()));
        LOGGER.log("Waiting for response from input stream");
        String response;
        try {
            while (!reader.ready()) {
                Thread.sleep(100);
            }
            response = reader.readLine();
            LOGGER.log("Response received");
        } catch (InterruptedException e) {
            LOGGER.log("Operation cancelled");
            return new byte[0];
        }
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
            LOGGER.log("Unexpected error: " + e.getMessage());
        }
    }
}
