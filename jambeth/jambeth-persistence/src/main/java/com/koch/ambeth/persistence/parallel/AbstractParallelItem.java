package com.koch.ambeth.persistence.parallel;

import java.util.Collection;

import com.koch.ambeth.util.collections.LinkedHashMap;

public abstract class AbstractParallelItem
{
	public final LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();

	public final LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit;

	public AbstractParallelItem(LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		this.sharedCascadeTypeToPendingInit = sharedCascadeTypeToPendingInit;
	}
}
