package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

/**
 * A {@link Runnable}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedRunnable {

    static CheckedRunnable of(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        return () -> runnable.run();
    }


    @SneakyThrows
    static void invoke(CheckedRunnable runnable) {
        runnable.run();
    }

    void run() throws Exception;
}
