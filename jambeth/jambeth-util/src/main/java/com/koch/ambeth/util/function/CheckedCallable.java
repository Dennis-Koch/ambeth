package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * A {@link Callable}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedCallable<R> {
    static <R> CheckedCallable<R> of(Callable<R> callable) {
        if (callable == null) {
            return null;
        }
        return () -> callable.call();
    }

    @SneakyThrows
    static <R> R invoke(CheckedCallable<R> callable) {
        return callable.call();
    }

    R call() throws Exception;
}
