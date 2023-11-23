package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;

/**
 * A {@link Consumer}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedConsumer<T> {
    static <T> CheckedConsumer<T> of(Consumer<T> consumer) {
        if (consumer == null) {
            return null;
        }
        return arg -> consumer.accept(arg);
    }

    @SneakyThrows
    static <T> void invoke(CheckedConsumer<T> consumer, T arg) {
        consumer.accept(arg);
    }

    void accept(T t) throws Exception;
}
