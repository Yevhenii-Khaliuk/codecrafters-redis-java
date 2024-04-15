package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskRunner {
    private static final Logger LOGGER = new Logger(TaskRunner.class);

    private final Callable<byte[]> task;
    private final long timeout;
//    private boolean finished;
//    private byte[] result;

    public TaskRunner(Callable<byte[]> task, long timeout) {
        this.task = task;
        this.timeout = timeout;
    }

    public byte[] run() throws TimeoutException {
//        Instant timedOut = Instant.now().plus(timeout);
        /*Thread thread = new Thread(() -> {
            try {
                result = task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            finished = true;
        });
        thread.start();*/

        /*while (true) {
            if (finished) {
                return result;
            }
            if (Instant.now().isAfter(timedOut)) {
                LOGGER.log("Operation timed out");
                thread.interrupt();
                throw new TimeoutException();
            }
        }*/

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        byte[] result = new byte[0];
        try {
            result = executorService.submit(task).get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log("Error during execution: " + e);
        }
        executorService.shutdownNow();
        executorService.close();
        return result;
    }
}
