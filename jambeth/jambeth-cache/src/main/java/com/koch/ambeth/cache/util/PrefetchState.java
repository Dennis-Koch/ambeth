package com.koch.ambeth.cache.util;

import com.koch.ambeth.merge.util.IPrefetchState;

public class PrefetchState implements IPrefetchState
{
	@SuppressWarnings("unused")
	private final Object hardRef;

	public PrefetchState(Object hardRef)
	{
		this.hardRef = hardRef;
	}
}
