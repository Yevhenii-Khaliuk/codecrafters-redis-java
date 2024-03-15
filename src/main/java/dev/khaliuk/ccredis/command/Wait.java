package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.ObjectFactory;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Wait extends AbstractHandler {
    private final AtomicInteger acknowledgedReplicasNumber = new AtomicInteger();

    public Wait(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        if (!(NumberUtils.isDigits(arguments[1]) && NumberUtils.isDigits(arguments[2]))) {
            throw new IllegalArgumentException(String.format("Digits expected: %s, %s", arguments[1], arguments[2]));
        }
        int expectedReplicasNumber = Integer.parseInt(arguments[1]);
        // TODO: stop waiting for futures, when reached expected replicas number
        long timeoutMillis = Long.parseLong(arguments[2]);
        System.out.println("Received WAIT arguments: replicas " + expectedReplicasNumber + ", timeout " + timeoutMillis);
        List<Socket> replicas = objectFactory.getApplicationProperties().getReplicas();
        System.out.println(LocalTime.now() + ": Found " + replicas.size() + " replicas");
        Stream<CompletableFuture<Void>> futures = replicas.stream()
                .map(replica -> CompletableFuture.runAsync(() -> getAcknowledgement(replica)));
        if (timeoutMillis > 0) {
            futures = futures.map(future ->
                    future.completeOnTimeout(null, timeoutMillis, TimeUnit.MILLISECONDS));
        }
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        int ackNumber = acknowledgedReplicasNumber.intValue();
        acknowledgedReplicasNumber.set(0);
        System.out.println(LocalTime.now() + ": Counter after all futures completed: " + ackNumber);
        return objectFactory.getProtocolSerializer().integer(ackNumber == 0 ? replicas.size() : ackNumber);
    }

    private void getAcknowledgement(Socket replica) {
        System.out.println(LocalTime.now() + ": Start sending ack request");
        byte[] command = objectFactory.getProtocolSerializer().array(List.of("REPLCONF", "GETACK", "*"));
        try {
            OutputStream outputStream = replica.getOutputStream();
            outputStream.write(command);
            outputStream.flush();
            DataInputStream inputStream = new DataInputStream(replica.getInputStream());
            String response = objectFactory.getProtocolDeserializer().parseInput(inputStream).getLeft();
            System.out.println(LocalTime.now() + ": Got response: " + response);
            acknowledgedReplicasNumber.incrementAndGet();
            // TODO: check offset if needed
        } catch (IOException e) {
            System.out.println("Acknowledgement error: " + e.getMessage());
        }
    }
}
