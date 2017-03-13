package com.koch.ambeth.service.cache;

public enum ClearAllCachesEvent
{
	instance;

	public static ClearAllCachesEvent getInstance()
	{
		return instance;
	}

	private ClearAllCachesEvent()
	{
		// intended blank
	}
}
