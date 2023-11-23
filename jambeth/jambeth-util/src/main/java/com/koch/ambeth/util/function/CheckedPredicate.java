package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.function.Predicate;

/**
 * A {@link Predicate}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedPredicate<T> {
    static <T> CheckedPredicate<T> of(Predicate<T> predicate) {
        if (predicate == null) {
            return null;
        }
        return arg -> predicate.test(arg);
    }

    @SneakyThrows
    static <T> boolean invoke(Predicate<T> predicate, T arg) {
        return predicate.test(arg);
    }

    boolean test(T t) throws Exception;
}
