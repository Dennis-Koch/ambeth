package com.koch.ambeth.ioc.util;

import java.util.Collection;



public abstract class IImmutableTypeSet {
	public abstract void addImmutableTypesTo(Collection<Class<?>> collection);

	public abstract boolean isImmutableType(Class<?> type);
}
