package com.koch.ambeth.service.proxy;

import java.lang.reflect.Method;

public class SimpleMethodLevelBehavior<T> implements IMethodLevelBehavior<T> {
	protected final T defaultBehaviour;

	public SimpleMethodLevelBehavior(T defaultBehaviour) {
		this.defaultBehaviour = defaultBehaviour;
	}

	@Override
	public T getDefaultBehaviour() {
		return defaultBehaviour;
	}

	@Override
	public T getBehaviourOfMethod(Method method) {
		return defaultBehaviour;
	}
}
