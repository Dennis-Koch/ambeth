package com.koch.ambeth.util;

import java.util.function.Supplier;

public class Once<T> implements Supplier<T> {
	public static <T> Supplier<T> once(Supplier<? extends T> calledOnceSupplier) {
		return new Once<>(calledOnceSupplier);
	}

	private T value;

	private volatile boolean onceCalled;

	private Supplier<? extends T> calledOnceSupplier;

	public Once(Supplier<? extends T> calledOnceSupplier) {
		ParamChecker.assertParamNotNull(calledOnceSupplier, "calledOnceSupplier");
		this.calledOnceSupplier = calledOnceSupplier;
	}

	@Override
	public T get() {
		if (onceCalled) {
			return value;
		}
		synchronized (this) {
			if (onceCalled) {
				return value;
			}
			value = calledOnceSupplier.get();
			calledOnceSupplier = null;
			onceCalled = true;
		}
		return value;
	}
}
