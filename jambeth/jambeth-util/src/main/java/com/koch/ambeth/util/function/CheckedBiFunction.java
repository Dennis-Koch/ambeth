package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.function.BiFunction;

/**
 * A {@link BiFunction}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {
    static <T, U, R> CheckedBiFunction<T, U, R> of(BiFunction<T, U, R> function) {
        if (function == null) {
            return null;
        }
        return (arg1, arg2) -> function.apply(arg1, arg2);
    }

    @SneakyThrows
    static <T, U, R> R invoke(CheckedBiFunction<T, U, R> function, T state1, U state2) {
        return function.apply(state1, state2);
    }

    R apply(T arg1, U arg2) throws Exception;
}
