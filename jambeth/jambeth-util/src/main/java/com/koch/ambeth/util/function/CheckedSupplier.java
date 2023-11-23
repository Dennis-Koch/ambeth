package com.koch.ambeth.util.function;

import lombok.SneakyThrows;

import java.util.function.Supplier;

/**
 * A {@link Supplier}-like interface which allows throwing checked exceptions.
 */
@FunctionalInterface
public interface CheckedSupplier<R> {
    static <R> CheckedSupplier<R> of(Supplier<R> supplier) {
        if (supplier == null) {
            return null;
        }
        return () -> supplier.get();
    }

    @SneakyThrows
    static <R> R invoke(CheckedSupplier<R> supplier) {
        return supplier.get();
    }

    R get() throws Exception;
}
