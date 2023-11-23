package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.function.Function;

/**
 * A {@link Function}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
    static <T, R> CheckedFunction<T, R> of(Function<T, R> function) {
        if (function == null) {
            return null;
        }
        return arg -> function.apply(arg);
    }

    @SneakyThrows
    static <R, S> R invoke(CheckedFunction<S, R> function, S state) {
        return function.apply(state);
    }

    R apply(T t) throws Exception;
}
