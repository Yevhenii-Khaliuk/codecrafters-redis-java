package dev.khaliuk.ccredis.config;

import java.time.LocalTime;

public class Logger {
    private final String className;

    public Logger(Class<?> classType) {
        this.className = classType.getSimpleName();
    }

    public void log(String message) {
        System.out.printf("[%s][%s][%s] %s%n", LocalTime.now(), Thread.currentThread().getName(), className, message);
    }
}
