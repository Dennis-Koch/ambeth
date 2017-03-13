package com.koch.ambeth.persistence.parallel;

import java.util.Collection;

import com.koch.ambeth.persistence.LoadMode;
import com.koch.ambeth.util.collections.LinkedHashMap;

public class ParallelLoadItem extends AbstractParallelItem
{
	public final Class<?> entityType;

	public final byte idIndex;

	public final Collection<Object> ids;

	public final LoadMode loadMode;

	public ParallelLoadItem(Class<?> entityType, byte idIndex, Collection<Object> ids, LoadMode loadMode,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		super(sharedCascadeTypeToPendingInit);
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.ids = ids;
		this.loadMode = loadMode;
	}
}